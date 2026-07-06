---
paths:
  - "src/main/resources/application.yml"
  - "src/main/resources/logback-spring.xml"
  - "src/main/java/**/exception/**"
  - "src/main/java/**/config/CamelBee*.java"
  - "src/main/java/**/utils/UuidResolver.java"
---
# Observability, Logging & Error Framework

This file documents how a CamelBee microservice surfaces what's happening internally — tracing, the debugger UI, MDC correlation, logging, and the error handler framework. These pieces work together; changing one in isolation usually breaks another.

---

## CamelBee Tracer

The tracer intercepts every route step and stores messages for the debugger UI. Control it via `application.yml`:

```yaml
camelbee:
  context-enabled: true        # master switch — enables CamelBee integration at all
  tracer-enabled: true          # captures messages for the UI
  notifier-enabled: true        # emits lifecycle events
  logging-enabled: true         # structured log on every route step
  tracer-max-idle-time: 60000   # auto-disable after N ms of inactivity (default 60s)
  tracer-max-messages-count: 10000  # ring buffer size per route
```

### Env var overrides

All of these accept env vars for production:

```bash
CAMELBEE_CONTEXT_ENABLED=true
CAMELBEE_TRACER_ENABLED=false        # disable in prod by default — perf overhead
CAMELBEE_TRACER_MAX_IDLE_TIME=30000
```

### Lifecycle

- Auto-disables after `tracer-max-idle-time` to limit memory growth — re-enable by hitting the route again
- Ring buffer: oldest messages evicted when count exceeds `tracer-max-messages-count`
- **Requires** `camelBeeRouteConfigurer.configureRoute(this)` as the first line of every route's `configure()` — skipping this means the route is invisible to the tracer even if it runs

---

## Debugger UI

Available at `http://localhost:8080/camelbee/index.html` when the app is running and `context-enabled: true`.

Features:
- **Topology** — visual graph of routes and endpoint connections
- **Message tracing** — inspect request/response bodies and headers per step
- **Stream playback** — replay captured exchanges
- **Health + metrics** — reads `/actuator/health` (SpringBoot) or `/q/health` (Quarkus)

Use `/cb-debug` to trigger a request through the UI and verify message flow visually — especially after editing a route.

### Two ways to run the stack for debugging

| Mode | Backends | Microservice |
|---|---|---|
| **Dockerized (what `/cb-debug` does)** | `docker compose up --build -d` — the root `docker-compose.yml` starts the app container AND `include`s `src/integration-test/resources/compose-backends.yml`, so all backend services come up in the same stack | Runs in Docker; UI served by the app container |
| **Host-run app (hot reload / IDE debugging)** | Start only the backends: `docker compose -f src/integration-test/resources/compose-backends.yml up -d` | Run on the host with `./mvnw quarkus:dev` — `application.yml` defaults already point at `localhost:<published-port>` |

In both modes the debugger UI is at `http://localhost:8080/camelbee/index.html`, and in both modes the backends come from the **same `compose-backends.yml`** that the integration-test `DockerComposeContainer` boots. One backend definition is shared by runtime debugging, integration tests, and black-box tests — which is exactly why replacing the compose-based test setup with backend-specific TestContainers classes is forbidden (see `test-patterns.md`): it would make the test environment diverge from the debug/runtime environment.

---

## MDC correlation

Every log line carries correlation fields so you can trace one request across multiple log events:

| MDC key | Source | Used for |
|---|---|---|
| `businessTransactionId` | `transactionId` request header, or `UuidResolver.resolveUuid(exchange)` if absent | Single end-to-end trace ID for one request |
| `tx_request_uri` | HTTP request URI | Correlates across multi-backend fan-out |
| `tx_routeId` | Route's `.routeId()` | Which route emitted the log line |
| `tx_content_type` | `Content-Type` header | Quick filter by format when debugging multi-format flows |

### UuidResolver pattern

Always use `UuidResolver.resolveUuid(exchange)` (static utility) at the first consumer route to obtain the transaction ID. It:
1. Reads the `transactionId` header if present
2. Generates a UUID if absent
3. Sets both the header AND MDC

Do **not** generate UUIDs ad-hoc in downstream routes — that breaks correlation.

---

## Logging configuration

### Quarkus logging

Configured in `application.yml` under `quarkus.log.*`:

```yaml
quarkus:
  log:
    level: INFO
    category:
      "io.camelbee":
        level: DEBUG
      "org.apache.camel":
        level: INFO
    console:
      format: "%d{yyyy-MM-dd HH:mm:ss,SSS} %-5p [%c{3.}] (%t) [${businessTransactionId:-}] %s%e%n"
```

For JSON logging in prod, enable `quarkus.log.console.json`. See `application.yml` for active config.

---

## Error handler framework

### The `GenericExceptionHandler`

Single CDI/Spring bean that defines the dead-letter strategy used by every consumer route:

```java
public class GenericExceptionHandler {
  public ExceptionBuilderConfigurer appErrorHandler() {
    return deadLetterChannel("direct:error")
        .useOriginalMessage()
        .logRetryAttempted(true)
        .maximumRedeliveries(0);
  }
}
```

Wired into consumers via `errorHandler(genericExceptionHandler.appErrorHandler())`. Never use this in central or producer routes — they use `errorHandler(noErrorHandler())` so exceptions propagate upward.

### The `ErrorMetaResolver` classification

Any exception that hits `direct:error` is inspected by `ErrorMetaResolver` and classified into an `ErrorMeta` record:

```java
public record ErrorMeta(String code, String message, int httpStatus) {}
```

The resolver maps Java exception types → `ErrorMeta`. For example:
- `JsonParseException` → `ERROR-JSON-001 / 400`
- `ConstraintViolationException` → `ERROR-VALIDATION-001 / 400`
- `InsufficientPrivilegesException` → `ERROR-AUTH010 / 403`
- anything else → `ERROR-UNKNOWN-001 / 500`

**Add a new error type:** extend `CamelbeeException`, add a new `ErrorCode`, handle it in `ErrorMetaResolver`.

### ResponseFormatter interface

Each interface protocol has its own formatter that converts `ErrorMeta` into the protocol's error shape:

| Interface | Formatter | Output shape |
|---|---|---|
| REST | `RestErrorResponseFormatter` | JSON body `{code, message}` + HTTP status |

The right formatter is selected by reading `Constants.ORIGINAL_INTERFACE_TYPE` (set by the consumer route) and dispatching accordingly.

### Adding a new response formatter

1. Implement `ResponseFormatter` (single `format(Exchange, ErrorMeta)` method)
2. Register as `@Component` (Spring) / `@ApplicationScoped` (Quarkus)
3. Update `direct:error` route to dispatch to it by interface type
4. Add an error-scenario test per existing formatter

---

## Exchange properties you can't skip

The tracer and debugger UI expect these properties to be set at known points — skipping any of them produces "empty" messages in the UI:

| Property (from `Constants`) | Set where | Why |
|---|---|---|
| `ORIGINAL_BODY` | First consumer step (before unmarshal) | UI shows the raw incoming body |
| `ORIGINAL_CONTENT_TYPE` | First consumer step | Multi-format routing + debugger display |
| `ORIGINAL_ACCEPT_CONTENT_TYPE` | First consumer step | Response marshaling target |
| `ACTUAL_RESPONSE_BODY` | End of producer route | UI shows backend response pre-marshaling |
| `ORIGINAL_INTERFACE_TYPE` | First consumer step | Error formatter dispatch |

These are set by `camelBeeRouteConfigurer.configureRoute(this)` + the standard consumer processor chain. If you're writing a new consumer from scratch, do NOT skip this initialization.

---

## `@ToString` is required on all model classes

The CamelBee tracer captures exchange bodies at every route step by calling `toString()`. If `toString()` is not implemented, the debugger UI and structured logs display unreadable object references (`ClassName@abc123`) instead of actual field values — making the debugger useless for that message type.

This applies to all three model layers:

| Layer | Package | Examples |
|-------|---------|---------|
| API models | `model/api/` | JSON/XML POJOs, event wrappers (`OrderEvent`), CSV records |
| Domain models | `model/domain/` | `Order`, `OrderItem`, `Error`, messaging event types |
| Infra models | `model/infra/` | `Purchase`, `PurchaseItem` (for every backend: SQL, JPA, MongoDB, Cassandra, DynamoDB, cache, messaging, SSE, CSV) |

**Generated templates already include `@ToString`.** The rule applies when you add a new model class manually:

```java
# Safe: no bidirectional references
@ToString
public class Order { ... }

# JPA bidirectional — exclude the @ManyToOne back-reference from the child side
# to avoid infinite recursion (PurchaseItem -> Purchase -> items -> PurchaseItem -> ...)
@ToString(exclude = "purchase")
public class PurchaseItem { ... }

# JPA parent side — optionally exclude the @OneToMany collection for brevity
@ToString(exclude = "items")
public class Purchase { ... }
```

**Maven-generated models** (OpenAPI Generator, Protobuf, Avro, JAXB, CXF Codegen) already have `toString()` from their respective generators — no action needed for those.

**Quick diagnostic:** if you see `ClassName@abc123` in the CamelBee UI, grep the class name — the Lombok `@ToString` annotation (or a manual `toString()` override) is missing from that class.

---

## Anti-patterns

| Wrong | Right |
|---|---|
| Generating UUID in every processor | Call `UuidResolver.resolveUuid(exchange)` at the first consumer step only |
| `log.info("processed " + order.getId())` in a hot path | `log.info("processed {}", order.getId())` — SLF4J placeholder |
| Writing to stdout/stderr directly | Always use SLF4J logger |
| `errorHandler(genericExceptionHandler.appErrorHandler())` in a central or producer route | `errorHandler(noErrorHandler())` — exceptions must propagate to the consumer's dead letter |
| Swallowing exceptions in `doTry/doCatch` without rethrowing in gRPC/WebSocket routes | Rethrow as `StatusRuntimeException` (gRPC) or send a text frame with the error (WebSocket) |
| Disabling the tracer in dev profile | Keep it on in dev — it's the fastest way to see what's happening |
| Enabling the tracer in prod without thinking | It costs memory + a small CPU hit per exchange — disable or reduce `tracer-max-messages-count` in high-throughput prod |