---
paths:
  - "src/main/java/**/routes/**"
  - "src/main/java/**/model/**"
  - "src/main/java/**/mapper/**"
---
# Architecture Standards

CamelBee-generated Apache Camel microservice (**QUARKUS**).

---

## Quick Reference

| Element | Standard |
|---------|----------|
| Package base | `com.mycompany.product.catalog` |
| Framework | **QUARKUS** |
| Consumer route class | `{Format}{Technology}ConsumerRoute extends RouteBuilder` |
| Central route class | `Central{Operation}Route extends RouteBuilder` |
| Producer route class | `{Technology}ProducerRoute extends RouteBuilder` |
| API mapper | `{Format}OrderMapper` (MapStruct, API model <-> Domain) |
| Infra mapper | `{Format}PurchaseMapper` (MapStruct, Domain <-> Infra) |
| Error handler | `GenericExceptionHandler` (dead-letter to `direct:error`) |
| Error processor | `GlobalErrorProcessor` (root cause extraction + ResponseFormatter dispatch) |
| Error classifier | `ErrorMetaResolver` (Exception -> ErrorMeta) |
| Config | `SharedMapperConfig` (MapStruct shared config) |

---

## Three-Layer Route Architecture

```
Consumer Routes (Interfaces)  --->  Central Routes (Orchestration)  --->  Producer Routes (Backends)
```

**Interfaces (Consumer Routes):** MCP

**Backends (Producer Routes):** REST,JPA

Each interface has a consumer route that receives requests, unmarshals, and routes to the central route. The central route orchestrates business logic and fans out to all configured backend producer routes.

### Layer Responsibilities

| Layer | Directory | Responsibility |
|-------|-----------|----------------|
| **Consumer** | `routes/consumer/{technology}/` | Unmarshal incoming data, validate, map API->Domain, route to central |
| **Central** | `routes/central/` | Business validation, store original body, fan-out to ALL configured backends |
| **Producer** | `routes/producer/{technology}/` | Map Domain->Infra, marshal, call backend, unmarshal response, map back |

### Data Flow

```
Incoming Request
    |
    v
[Consumer Route]
    1. Unmarshal (JSON/XML/Proto/Avro/SOAP/CSV)
    2. Bean validation: .to("bean-validator://camelbee")
    3. Map API model -> Domain model (via API mapper)
    4. Store content-type headers (ORIGINAL_ACCEPT_CONTENT_TYPE)
    5. Route to: direct:central{Operation}
    |
    v
[Central Route]
    1. Business validation (e.g., order items not empty)
    2. Store original body: .setProperty(Constants.ORIGINAL_BODY, body())
    3. Fan-out to ALL configured backends:
       .to("direct:{operation}Rest")
       .to("direct:{operation}Kafka")
       .to("direct:{operation}Amqp")
       ... (one per enabled backend)
    4. Set response from ACTUAL_RESPONSE_BODY property
    |
    v
[Producer Route]
    1. Retrieve original body from ORIGINAL_BODY property
    2. Map Domain model -> Infra model (via Infra mapper)
    3. Marshal to backend format
    4. Set headers (Content-Type, HTTP method, auth)
    5. Call backend endpoint
    6. Unmarshal response
    7. Map Infra response -> Domain model
    8. Store in ACTUAL_RESPONSE_BODY property

NOTE: The generated producer routes are independent — each reads
the original incoming request body (ORIGINAL_BODY) and calls its backend.
In real microservices, this flow can be customized:
- A producer can use the RESPONSE from a previous backend call as its input
  (e.g., call REST backend first, then use its response to call Kafka)
- The central route can transform or enrich data between backend calls
- ACTUAL_RESPONSE_BODY can be set by any producer — the last one wins,
  or the central route can merge responses from multiple backends
- ORIGINAL_BODY is a convenience, not a mandate — skip it if not needed
    |
    v
[Consumer Route - Response]
    1. Map Domain model -> API response model
    2. Marshal to client's requested format (content negotiation)
    3. Return response
```

---

## Three-Tier Model Architecture

```
model/
+-- api/            <-- API-facing models (format-specific)
|
+-- domain/         <-- Core business models (format-agnostic)
|   +-- Order.java
|   +-- OrderItem.java
|   +-- StatusEnum (PENDING, CONFIRMED, PROCESSING, SHIPPED, ...)
|
+-- infra/          <-- Infrastructure models (backend-specific)
    +-- json/       Purchase.java, PurchaseItem.java
    +-- jpa/        Purchase.java entity (JPA backend)
```

### Model Transformation Flow

```
API Model (e.g., json.Order)                       Infra Model (e.g., json.Purchase)
          |                                                    ^
          | API Mapper (JsonOrderMapper)                       | Infra Mapper (JsonPurchaseMapper)
          v                                                    |
       Domain Model (Order)  -------->  Domain Model (Order) --+
```

| Boundary | Mapper Type | Example | Direction |
|----------|-------------|---------|-----------|
| Consumer (inbound) | API mapper | `JsonOrderMapper.jsonToDomainOrder()` | API -> Domain |
| Consumer (response) | API mapper | `JsonOrderMapper.domainToJsonOrder()` | Domain -> API |
| Producer (outbound) | Infra mapper | `JsonPurchaseMapper.domainOrderToJsonPurchase()` | Domain -> Infra |
| Producer (response) | Infra mapper | `JsonPurchaseMapper.jsonPurchaseToDomainOrder()` | Infra -> Domain |

**Key field mapping**: Domain uses `orderDate`, backend infra uses `purchaseDate` — mappers handle the translation.

### How `.convertBodyTo()` Works with MapStruct

Routes use `.convertBodyTo(Order.class)` and `.convertBodyTo(Purchase.class)` extensively. This works because:

1. **MapStruct generates implementations** at compile time from `@Mapper` interfaces
2. **Spring/CDI registers them as beans** automatically (`@Component` in SpringBoot, `@Singleton` in Quarkus)
3. **Camel's type conversion** discovers these beans and uses them for `.convertBodyTo()` calls

```
.convertBodyTo(Order.class)  // Camel finds the mapper bean that can convert current body type → Order
```

**No explicit TypeConverter registration is needed.** The `SharedMapperConfig` with `componentModel = "spring"` (or `"cdi"`) makes this automatic.

**When adding a new domain model**, you must:
1. Create the MapStruct mapper interface with `@Mapper(config = SharedMapperConfig.class, ...)`
2. Define both forward and reverse mapping methods
3. Ensure the mapper is in a package scanned by Spring/CDI
4. The `.convertBodyTo()` calls will work automatically after `./mvnw compile` generates the implementations

**Explicit vs implicit mapping**: Routes can also call mappers directly in `.process()` blocks instead of `.convertBodyTo()`. Both patterns are used:
```java
// Implicit (via type conversion) — simpler
.convertBodyTo(Order.class)

// Explicit (in process block) — when you need more control
.process(e -> {
    var purchase = e.getIn().getBody(Purchase.class);
    e.getIn().setBody(jsonPurchaseMapper.jsonPurchaseToDomainOrder(purchase));
})
```

---

## Mapper Architecture

### API Mappers (`mapper/api/`)

Map between format-specific API models and the Domain model.

```java
@Mapper(config = SharedMapperConfig.class,
        collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface JsonOrderMapper {

    // Inbound: API -> Domain
    Order jsonToDomainOrder(JsonOrder order);
    OrderItem jsonToDomainOrderItem(JsonOrderItem item);

    // Outbound: Domain -> API
    JsonOrder domainToJsonOrder(Order order);
    JsonOrderItem domainToJsonOrderItem(OrderItem item);

    // Batch
    List<Order> jsonToDomainOrders(List<JsonOrder> orders);
    List<JsonOrder> domainToJsonOrders(List<Order> orders);
}
```

### Infrastructure Mappers (`mapper/infra/`)

Map between Domain model and backend-specific infrastructure models.

```java
@Mapper(config = SharedMapperConfig.class,
        collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface JsonPurchaseMapper {

    // Domain -> Backend
    @Mapping(source = "orderDate", target = "purchaseDate")
    Purchase domainOrderToJsonPurchase(Order order);

    // Backend -> Domain
    @Mapping(source = "purchaseDate", target = "orderDate")
    Order jsonPurchaseToDomainOrder(Purchase purchase);

    // Items
    PurchaseItem domainOrderItemToJsonPurchaseItem(OrderItem item);
    OrderItem jsonPurchaseItemToDomainOrderItem(PurchaseItem item);

    // Batch
    List<Order> jsonPurchasesToDomainOrders(List<Purchase> purchases);
}
```

### Error Mappers (`mapper/api/{Format}ErrorMapper`)

Convert `ErrorMeta` to format-specific error response models.

| Mapper | Output |
|--------|--------|
| `JsonErrorMapper` | `json.Error` |
| `XmlErrorMapper` | `xml.Error` |
| `ProtoErrorMapper` | `proto.Error` |
| `AvroErrorMapper` | `avro.Error` |
| `SoapErrorMapper` | SOAP Fault |
| `GrpcErrorMapper` | gRPC Status |

### SharedMapperConfig

```java
@MapperConfig(componentModel = "cdi",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SharedMapperConfig { }
```

---

## Package Structure

```
com.mycompany.product.catalog/
+-- productCatalogServiceApplication.java
+-- config/
|   +-- SharedMapperConfig.java
|   +-- DataSourceConfig.java           (if SQL/JPA)
|   +-- JpaComponentConfig.java         (if JPA)
|   +-- SoapEndpointConfig.java         (if SOAP interface)
|   +-- SoapBackendConfig.java          (if SOAP backend)
|   +-- RabbitmqConfig.java             (if RabbitMQ)
|   +-- AmqpConfig.java                 (if AMQP)
|   +-- MonitoringConfig.java
|   +-- ReflectionConfig.java           (Quarkus only)
|   +-- RegisterCustomModuleCustomizer.java
|
+-- constants/
|   +-- Constants.java
|
+-- exception/
|   +-- GenericExceptionHandler.java    (dead-letter channel builder)
|   +-- GlobalErrorProcessor.java       (root cause + dispatch)
|   +-- ErrorMetaResolver.java          (exception classification)
|   +-- ErrorMeta.java                  (record: code, message, status)
|   +-- response/
|       +-- ResponseFormatter.java      (interface)
|       +-- RestErrorResponseFormatter.java
|       +-- SoapErrorResponseFormatter.java
|       +-- GraphqlErrorResponseFormatter.java
|       +-- GrpcErrorResponseFormatter.java
|       +-- WebsocketErrorResponseFormatter.java
|       +-- McpErrorResponseFormatter.java
|       +-- DefaultErrorResponseFormatter.java
|
+-- mapper/
|   +-- api/                            (API <-> Domain)
|   |   +-- JsonOrderMapper.java
|   |   +-- XmlOrderMapper.java
|   |   +-- ProtoOrderMapper.java
|   |   +-- AvroOrderMapper.java
|   |   +-- ...
|   +-- infra/                          (Domain <-> Infrastructure)
|       +-- JsonPurchaseMapper.java
|       +-- XmlPurchaseMapper.java
|       +-- JpaPurchaseMapper.java
|       +-- SqlPurchaseMapper.java
|       +-- ...
|
+-- model/
|   +-- api/                            (format-specific API models)
|   +-- domain/                         (core business models)
|   +-- infra/                          (backend-specific models)
|
+-- routes/
    +-- consumer/                       (interface entry points)
    |   +-- rest/
    |   +-- soap/
    |   +-- kafka/
    |   +-- amqp/
    |   +-- ...
    +-- central/                        (business orchestration)
    |   +-- CentralCreateOrderRoute.java
    |   +-- CentralListOrdersRoute.java
    |   +-- CentralGetOrderRoute.java
    |   +-- CentralReplaceOrderRoute.java
    |   +-- CentralUpdateOrderRoute.java
    |   +-- CentralDeleteOrderRoute.java
    |   +-- CentralCreateOrdersBatchRoute.java
    +-- producer/                       (backend calls)
        +-- rest/
        +-- soap/
        +-- kafka/
        +-- amqp/
        +-- ...
```

---

## Error Handling Architecture

### Multi-Interface Error Flow

This microservice supports **multiple interfaces simultaneously**. The error handling system must return errors in the correct protocol and format for each interface.

```
Exception in any route
    |
    v
Dead Letter Channel --> direct:error (GenericExceptionHandler.appErrorHandler())
    |
    v
GlobalErrorProcessor.process(exchange)
    1. Extract root cause (unwrap CamelExchangeException, up to 10 levels)
    2. Handle special case: IOException wrapping real cause
    3. Resolve to ErrorMeta via ErrorMetaResolver
    4. Log: 500+ = ERROR, 400+ = WARN, else DEBUG
    5. Iterate sorted ResponseFormatters (by priority)
    6. First formatter where supports(exchange) == true handles it
    |
    v
ResponseFormatter dispatch (by originating endpoint):
    +-- MCP                      --> McpErrorResponseFormatter
    +-- Default                  --> DefaultErrorResponseFormatter (fallback)
```

### ErrorMeta Record

```java
public record ErrorMeta(String code, String message, int status) {
    static final int BAD_REQUEST = 400;
    static final int UNAUTHORIZED = 401;
    static final int FORBIDDEN = 403;
    static final int NOT_FOUND = 404;
    static final int INTERNAL_SERVER_ERROR = 500;
}
```

### ErrorMetaResolver Classification

| Exception Type | HTTP Status | Error Code |
|----------------|-------------|------------|
| `BeanValidationException` | 400 | BAD_REQUEST |
| `JsonParseException`, `InvalidFormatException` | 400 | BAD_REQUEST |
| `UnmarshalException` (XML) | 400 | BAD_REQUEST |
| `InvalidTokenException`, `TokenExpiredException` | 401 | UNAUTHORIZED |
| `InsufficientPrivilegesException` | 403 | FORBIDDEN |
| `DataNotFoundException` | 404 | NOT_FOUND |
| `HttpOperationFailedException` | from response | from response |
| `HttpHostConnectException` | 500 | INTERNAL_SERVER_ERROR |
| JPA/SQL/JDBC exceptions | 500 | INTERNAL_SERVER_ERROR |
| gRPC `StatusRuntimeException` | mapped from gRPC code | gRPC status |
| All other | 500 | INTERNAL_SERVER_ERROR |

**Security rule**: Error messages returned to clients are **sanitized** — generic messages only. Actual exception details are logged server-side.

### ResponseFormatter Interface

```java
public interface ResponseFormatter {
    boolean supports(Exchange exchange);
    void format(Exchange exchange, ErrorMeta meta);
    default int getPriority() { return 100; }  // lower = higher priority
}
```

### GenericExceptionHandler

```java
// Dead-letter channel configuration
// Quarkus: errorHandler(deadLetterChannel("direct:error").useOriginalMessage().logExhausted(false))
```

The error handler route (`direct:error`) in GenericExceptionHandler:
1. Processes via `GlobalErrorProcessor`
2. For REST: marshals error in requested format (JSON/XML/Proto/Avro) based on `ORIGINAL_ACCEPT_CONTENT_TYPE`
3. For JMS/AMQP: marks transaction rollback via `shouldMarkRollbackOnly()`
4. For MCP: formats MCP error response

---

## Exchange Property Management

| Property | Set By | Used By | Purpose |
|----------|--------|---------|---------|
| `ORIGINAL_BODY` | Central route | Producer route | Preserve request body before backend transforms it |
| `ORIGINAL_ROUTE_BODY` | Consumer route | Response mapping | Original body at route entry |
| `ORIGINAL_REQUEST_BODY` | Consumer route | Error handling | Track transformations |
| `ACTUAL_RESPONSE_BODY` | Producer route | Central/Consumer | Backend response for final response mapping |

**These properties are conventions, not rigid rules.** The generated code uses them in a simple pattern where each producer independently reads `ORIGINAL_BODY`. In your microservice, you can adapt the flow: pass data between producers via body or exchange properties, merge responses, chain backends where one's output feeds the next, or skip `ORIGINAL_BODY` entirely if the logic doesn't need it. The only property the consumer route relies on for the final response is `ACTUAL_RESPONSE_BODY`.
| `ORIGINAL_CONTENT_TYPE` | Consumer route | Error handler | Request Content-Type |
| `ORIGINAL_ACCEPT_CONTENT_TYPE` | Consumer route | Error handler, response | Accept header for content negotiation |
| `AGGREGATED_BATCH_ORDERS` | Batch routes | Central | Aggregated batch items |

**Rule**: Use exchange **properties** (not headers) for cross-route data. Properties survive the full exchange lifecycle; headers may be lost during backend calls.

---

## Consumer Route Patterns

---

## Central Route Patterns

Central routes are the **orchestration layer**. They validate and fan-out to all configured backends.

```java
from("direct:centralCreateOrder").routeId("centralCreateOrderRoute")
    .errorHandler(noErrorHandler())  // propagate to global handler
    .process(exchange -> {
        // Business validation
        Order order = exchange.getIn().getBody(Order.class);
        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new ValidationException(exchange, "Order items cannot be empty!");
        }
    })
    .setProperty(Constants.ORIGINAL_BODY, body())
    // Fan-out to ALL configured backends
    .to("direct:createOrderRest").id("createOrderRestEndpoint")
    .to("direct:createOrderSoap").id("createOrderSoapEndpoint")
    .to("direct:createOrderKafka").id("createOrderKafkaEndpoint")
    .to("direct:createOrderAmqp").id("createOrderAmqpEndpoint")
    // ... one .to() per enabled backend + operation
    // Set final response from backend
    .setBody(exchangeProperty(Constants.ACTUAL_RESPONSE_BODY));
```

**Key rules**:
- `noErrorHandler()` — exceptions propagate to the consumer's global error handler
- `ORIGINAL_BODY` stored before any backend call
- Each backend is wired with `.to("direct:{operation}{Backend}").id("{operation}{Backend}Endpoint")`
- The `.id()` is required for unit test mocking via `weaveById()`

---

## Producer Route Patterns

### REST Backend

```java
from("direct:createOrderRest").routeId("createOrderRestRoute")
    .errorHandler(noErrorHandler())
    .removeHeader(Exchange.HTTP_PATH)
    .removeHeader(Exchange.HTTP_URL)
    .setHeader(Exchange.HTTP_METHOD, constant("POST"))
    .setHeader(Exchange.CONTENT_TYPE, constant(APPLICATION_JSON))
    .setHeader(HttpHeaders.ACCEPT, constant(APPLICATION_JSON))
    .convertBodyTo(Purchase.class)
    .marshal().json()  // SpringBoot; Quarkus: .marshal(dataFormat)
    .to("http:{{backend-purchase-rest-api.url}}?bridgeEndpoint=true")
    .unmarshal().json(JsonLibrary.Jackson, Purchase.class)  // SpringBoot; Quarkus: .unmarshal(purchaseFormat)
    .convertBodyTo(Order.class)
    .setProperty(Constants.ACTUAL_RESPONSE_BODY, body());
```

### Header Cleanup Before HTTP Calls

```java
// MUST remove inherited headers before HTTP backend calls
.removeHeader(Exchange.HTTP_PATH)
.removeHeader(Exchange.HTTP_URL)
```

Without this, inherited headers from the consumer route override the backend URL.

---

## Content Negotiation

Multi-format consumers (REST) negotiate response format based on the `Accept` header.

| Accept Header | Response Format | Marshaler |
|---------------|----------------|-----------|
| `application/json` | JSON | `.marshal().json()` |
| `application/xml` | XML | `.marshal().jaxb()` |
| `application/protobuf` | Protocol Buffers | `.marshal().protobuf()` |
| `application/avro` | Apache Avro | `.marshal().avro(AvroLibrary.ApacheAvro)` |

The `ORIGINAL_ACCEPT_CONTENT_TYPE` property is set at the consumer and used by both the response path and the error handler to marshal in the correct format.

---

## Framework Differences

| Aspect | SpringBoot | Quarkus |
|--------|-----------|---------|
| DI Annotation | `@Component` | `@ApplicationScoped` |
| Bean definition | `@Bean` | CDI produces |
| MapStruct config | `componentModel = "spring"` | `componentModel = "cdi"` |
| JSON marshaling | `.marshal().json()` | `.marshal(jacksonDataFormat)` with explicit ObjectMapper |
| Error handler bean | `@Bean DeadLetterChannelBuilder` | inline `deadLetterChannel()` |
| Injection strategy | `InjectionStrategy.CONSTRUCTOR` | default CDI |
| Collection injection | `List<ResponseFormatter>` | `Instance<ResponseFormatter>` |
| Native compilation | N/A | Supported via `-Pnative` |

### Quarkus ObjectMapper Pattern

Quarkus requires explicit `JacksonDataFormat` setup:

```java
@Inject
ObjectMapper objectMapper;

JacksonDataFormat orderFormat;
JacksonDataFormat purchaseFormat;

@Override
public void configure() {
    orderFormat = new JacksonDataFormat(objectMapper, Order.class);
    purchaseFormat = new JacksonDataFormat(objectMapper, Purchase.class);

    from("direct:createOrderRest")
        .marshal(orderFormat)    // NOT .marshal().json()
        // ...
        .unmarshal(purchaseFormat);
}
```

---

## Route ID and Endpoint ID Conventions

### Route IDs (Required on all routes)

| Layer | Pattern | Example |
|-------|---------|---------|
| Consumer | `{format}{Technology}{Operation}Route` | `jsonKafkaCreateOrderRoute` |
| Central | `central{Operation}Route` | `centralCreateOrderRoute` |
| Producer | `{operation}{Technology}Route` | `createOrderRestRoute` |

### Endpoint IDs (Required on backend calls)

| Pattern | Example | Purpose |
|---------|---------|---------|
| `{operation}{Technology}Endpoint` | `createOrderRestEndpoint` | Central route fan-out targets |
| `{operation}{Technology}BackendEndpoint` | `createOrderRestBackendEndpoint` | Producer HTTP/external calls |

**Why IDs matter**:
- Unit tests use `weaveById()` to mock endpoints
- Integration tests use `AdviceWith` to replace endpoints
- CamelBee topology visualization uses route/endpoint IDs

---

## Operations Reference

| Code | Operation | HTTP Method | Central Route |
|------|-----------|------------|---------------|
| CRO | CreateOrder | POST | `direct:centralCreateOrder` |
| LSO | ListOrders | GET | `direct:centralListOrders` |
| GEO | GetOrder | GET | `direct:centralGetOrder` |
| REO | ReplaceOrder | PUT | `direct:centralReplaceOrder` |
| UPO | UpdateOrder | PATCH | `direct:centralUpdateOrder` |
| DEO | DeleteOrder | DELETE | `direct:centralDeleteOrder` |
| CRB | CreateOrdersBatch | POST | `direct:centralCreateOrdersBatch` |

---

## Active Configuration

### Interfaces (Consumers)

- **MCP**

### Backends (Producers)

- **REST**
- **JPA**

---

## Testing Architecture

| Level | Location | What It Tests | Dependencies |
|-------|----------|---------------|-------------|
| **Unit** | `src/test/` | Mapper transformations, individual route logic | Mocked backends via `weaveById()` |
| **Integration** | `src/integration-test/` | Full route flow per interface with real backends | TestContainers (Docker) |
| **Black-box** | `src/black-box-test/` | Fully dockerized end-to-end via actual protocol | Docker Compose |

### Test Commands

```bash
# Unit tests
./mvnw test

# Integration tests (requires Docker)
./mvnw jacoco:prepare-agent failsafe:integration-test failsafe:verify

# Black-box tests (fully dockerized)
./mvnw package -DskipTests
./mvnw verify -Pblack-box-test

# Native black-box tests
./mvnw verify -Pblack-box-test -Dbbtest.native=true
```

---

## Anti-Patterns

| Wrong | Right |
|-------|-------|
| Business logic in consumer route | Business logic in central route |
| Direct backend call from consumer | Route through central for orchestration |
| Headers for cross-route data | Exchange properties (survive full lifecycle) |
| Missing `.routeId()` | Always set route ID |
| Missing `.id()` on backend endpoints | Required for `weaveById()` testing |
| `http://` in Camel HTTP URLs | `http:` (no `://`) — Camel component syntax |
| Missing `bridgeEndpoint=true` | Required for dynamic HTTP URLs |
| Missing header cleanup before backend HTTP calls | Remove `HTTP_PATH`, `HTTP_URL` before backend calls |
| Hard-coded backend URLs | Use `{{config.property}}` placeholders |
| Catching exceptions in routes | Let global error handler manage via dead-letter channel |
| Returning unsanitized error messages | Use ErrorMetaResolver — log details server-side only |
| Modifying body without saving original | `.setProperty(Constants.ORIGINAL_BODY, body())` first |
| SpringBoot `.marshal().json()` in Quarkus | Use `JacksonDataFormat` with injected ObjectMapper |
| Multiple mappers in one class | Separate API mappers and Infra mappers per format |