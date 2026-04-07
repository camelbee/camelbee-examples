---
paths:
  - "src/main/java/**/routes/**"
  - "src/main/resources/application*.yml"
---
# Camel 4.x Standards

CamelBee-specific Apache Camel DSL patterns and conventions (**QUARKUS**).

---

## Quick Reference

| Element | Standard |
|---------|----------|
| Route init | `CamelBeeRouteConfigurer.configureRoute(this)` first, then `errorHandler(...)` |
| Error handler | `errorHandler(genericExceptionHandler.appErrorHandler())` (consumers) |
| No error handler | `errorHandler(noErrorHandler())` (central + producers) |
| Route ID | `.routeId("{format}{Technology}{Operation}Route")` — always required |
| Endpoint ID | `.id("{operation}{Technology}Endpoint")` — required on backend calls |
| Bean validation | `.to("bean-validator://camelbee")` |
| Original body | `.setProperty(Constants.ORIGINAL_BODY, body())` |
| Response body | `.setProperty(Constants.ACTUAL_RESPONSE_BODY, response)` |
| Dynamic HTTP | `.to("http:{{backend-purchase-rest-api.url}}?bridgeEndpoint=true")` |

---

## Route Initialization

Every route class follows this initialization order:

```java
@ApplicationScoped
public class MyConsumerRoute extends RouteBuilder {

    @Inject GenericExceptionHandler genericExceptionHandler;
    @Inject CamelBeeRouteConfigurer camelBeeRouteConfigurer;
    @Inject JsonOrderMapper jsonOrderMapper;

    @Override
    public void configure() {
        // 1. CamelBee configuration (stream caching, MDC, UnitOfWork)
        camelBeeRouteConfigurer.configureRoute(this);
        // 2. Error handler
        errorHandler(genericExceptionHandler.appErrorHandler());
        // 3. Route definitions
        from(...)
    }
}
```

| Order | Statement | Purpose |
|-------|-----------|---------|
| 1 | `camelBeeRouteConfigurer.configureRoute(this)` | Stream caching, MDC logging, CamelBeeUnitOfWork |
| 2 | `errorHandler(...)` | Global or no-error-handler per layer |
| 3 | Route definitions | Business logic |

---

## Error Handler Strategy by Layer

| Layer | Error Handler | Why |
|-------|--------------|-----|
| **Consumer** | `errorHandler(genericExceptionHandler.appErrorHandler())` | Dead-letter channel to `direct:error`, formats response per protocol |
| **Central** | `errorHandler(noErrorHandler())` | Propagate to consumer's handler |
| **Producer** | `errorHandler(noErrorHandler())` | Propagate to consumer's handler |

**Exception**: gRPC and WebSocket consumers use `doTry/doCatch` because these protocols cannot auto-respond to errors via the dead-letter channel.

---

## Marshaling & Unmarshaling

### JSON

```java
// Setup in configure()
JacksonDataFormat orderFormat = new JacksonDataFormat(objectMapper, Order.class);
// Unmarshal
.unmarshal(orderFormat)
// Marshal
.marshal(orderFormat)
```

**Quarkus requires explicit JacksonDataFormat** — never use `.marshal().json()` in Quarkus.







---

## Consumer Route Patterns by Protocol


#### Multi-Format Choice Routing

When REST supports **multiple formats** (JSON + XML + Proto + Avro), the consumer uses dedicated choice routes instead of inline unmarshal. The decision is made at project generation time:

- **Single format** → unmarshal directly inline (as shown above)
- **Multiple formats** → route to `direct:unmarshalOrder` / `direct:marshalOrder`

```java
// Multi-format: route to choice handler instead of inline unmarshal
from("direct:createOrder")
    .routeId("createOrderOperationRoute")
    .process(this::setOriginalContentTypeProperties)
    .to("direct:unmarshalOrder")  // choice-based unmarshal
    .convertBodyTo(Order.class)
    .to("direct:centralCreateOrder")
    .to("direct:marshalOrder")   // choice-based marshal
    .process(this::setOriginalContentTypePropertiesBack)
    .setHeader(HTTP_RESPONSE_CODE, constant(201))
```

The `direct:unmarshalOrder` route selects the unmarshaler based on `ORIGINAL_CONTENT_TYPE`:
```java
from("direct:unmarshalOrder")
    .choice()
        .when(exchangeProperty(ORIGINAL_CONTENT_TYPE).contains(APPLICATION_JSON))
            .unmarshal().json(JsonLibrary.Jackson, Order.class)
            .to("bean-validator://camelbee")
        .when(exchangeProperty(ORIGINAL_CONTENT_TYPE).contains(APPLICATION_XML))
            .unmarshal().jaxb("com.mycompany.product.catalog.model.api.xml")
        .when(exchangeProperty(ORIGINAL_CONTENT_TYPE).contains(APPLICATION_PROTOBUF))
            .convertBodyTo(byte[].class)
            .unmarshal().protobuf(Order.getDefaultInstance())
        .when(exchangeProperty(ORIGINAL_CONTENT_TYPE).contains(APPLICATION_AVRO))
            .convertBodyTo(byte[].class)
            .unmarshal().avro(AvroLibrary.ApacheAvro, Order.class)
    .end();
```

The `direct:marshalOrder` route selects the marshaler based on `ORIGINAL_ACCEPT_CONTENT_TYPE`:
```java
from("direct:marshalOrder")
    .choice()
        .when(exchangeProperty(ORIGINAL_ACCEPT_CONTENT_TYPE).contains(APPLICATION_JSON))
            .convertBodyTo(JsonOrder.class)
            .marshal().json()
        .when(exchangeProperty(ORIGINAL_ACCEPT_CONTENT_TYPE).contains(APPLICATION_XML))
            .convertBodyTo(XmlOrder.class)
            .marshal().jaxb()
        .when(exchangeProperty(ORIGINAL_ACCEPT_CONTENT_TYPE).contains(APPLICATION_PROTOBUF))
            .convertBodyTo(ProtoOrder.class)
            .marshal().protobuf()
        .when(exchangeProperty(ORIGINAL_ACCEPT_CONTENT_TYPE).contains(APPLICATION_AVRO))
            .convertBodyTo(AvroOrder.class)
            .marshal().avro(AvroLibrary.ApacheAvro)
    .end();
```

**Similar choice routes exist for lists**: `direct:unmarshalOrders` / `direct:marshalOrders` handle the ListOrders and CreateOrdersBatch operations.






### MCP Consumer (Model Context Protocol)

```java
from("direct:mcpCreateOrder")
    .routeId("mcpCreateOrderRoute")
    .process(e -> {
        // OAuth validation (role + scope)
    })
    .to("direct:centralCreateOrder");
```


---

## Producer Route Patterns by Protocol

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
    .marshal(purchaseFormat)
    .to("http:{{backend-purchase-rest-api.url}}?bridgeEndpoint=true")
    .unmarshal(purchaseFormat)
    .convertBodyTo(Order.class)
    .setProperty(Constants.ACTUAL_RESPONSE_BODY, body());
```


### JPA Backend

```java
from("direct:createOrderJpa").routeId("createOrderJpaRoute")
    .errorHandler(noErrorHandler())
    .process(e -> {
        Order order = e.getProperty(Constants.ORIGINAL_BODY, Order.class);
        e.getMessage().setBody(jpaPurchaseMapper.domainOrderToJpaPurchase(order));
    })
    .to("jpa-{{jpaVendor}}:com.mycompany.product.catalog.model.infra.jpa.Purchase")
        .id("createOrderJpaEndpoint");
```

Named queries for reads:
```java
.to("jpa:Purchase?namedQuery=Purchase.findPurchaseBySalesChannelAndId&singleResult=true")
```








---

## Bean Validation

```java
// Standard validation on JSON body (after unmarshal, before mapping)
.to("bean-validator://camelbee")
```

Validation annotations on API model classes (JSR-303):
```java
public class Order {
    @NotNull private String salesChannel;
    @NotEmpty private List<OrderItem> items;
    @Size(max = 100) private String description;
}
```

**Where to validate**:
- Consumer route: after unmarshal, before API->Domain mapping
- Central route: business rules (e.g., items not empty) via process()
- Bean validator is NOT used on central/producer routes

---

## OAuth/JWT Security Pattern

```java
// 1. Validate JWT token
.to("direct:validateJWT")

// 2. Check roles
.process(exchange -> {
    if (!JwtAuthorizationUtils.hasAllRoles(exchange, "app-service", "{operation}")) {
        throw new InsufficientPrivilegesException("ERROR-AUTH010", "Insufficient privileges");
    }
})

// 3. Check scopes
.process(exchange -> {
    if (!JwtAuthorizationUtils.hasScope(exchange, "camelbee-scope")) {
        throw new InsufficientPrivilegesException("ERROR-AUTH011", "Insufficient privileges");
    }
})
```

Applied to: REST, GraphQL, MCP, WebSocket consumers (when OAuth is enabled).

---

## HTTP Backend Call Patterns

### Header Cleanup (Required)

```java
// MUST remove before every HTTP backend call
.removeHeader(Exchange.HTTP_PATH)
.removeHeader(Exchange.HTTP_URL)
```

Without cleanup, inherited headers from the consumer override the backend URL.

### HTTP Method Setting

| Operation | HTTP Method |
|-----------|-------------|
| CRO (Create) | `constant("POST")` |
| LSO (List) | `constant("GET")` |
| GEO (Get) | `constant("GET")` |
| REO (Replace) | `constant("PUT")` |
| UPO (Update) | `constant("PATCH")` |
| DEO (Delete) | `constant("DELETE")` |

### Dynamic URL with Path Parameters

```java
.setHeader(Exchange.HTTP_METHOD, constant("GET"))
.removeHeader(Exchange.HTTP_URL)
.setHeader(Exchange.HTTP_PATH, simple("${header.id}"))
.setHeader("CamelHttpQuery", simple("salesChannel=${header.salesChannel}"))
.to("http:{{backend-purchase-rest-api.url}}?bridgeEndpoint=true")
```

| Parameter | Purpose |
|-----------|---------|
| `http:` (no `://`) | Camel HTTP component syntax |
| `{{property}}` | Camel property placeholder (from application.yml) |
| `${header.id}` | Simple expression for path parameter |
| `bridgeEndpoint=true` | Prevent URL override from Exchange headers |

---

## doTry/doCatch Pattern

Used when the dead-letter channel cannot send protocol-appropriate error responses.



### Error Handling by Protocol

| Protocol | Error Mechanism |
|----------|----------------|

---

## CamelBee-Specific Features

### CamelBeeRouteConfigurer

Called first in every route's `configure()` method. Enables:
- **Stream caching** — allows body to be read multiple times
- **MDC logging** — correlation IDs in log output
- **CamelBeeUnitOfWork** — request/response tracing for CamelBee topology visualization

### CamelBee Tracer

Configuration in `application.yml`:
```yaml
camelbee:
  tracer-enabled: true
  tracer-max-idle-time: 60000
  tracer-max-messages-count: 10000
```

Intercepts messages at every route step for the CamelBee UI topology view.

### UuidResolver

Static utility for transaction ID management:
```java
String transactionId = UuidResolver.resolveUuid(exchange);
// Returns existing transactionId from header or generates new UUID
```

---

## Configuration Patterns

### Property Placeholders

```java
// In routes: double curly braces
.to("http:{{backend-purchase-rest-api.url}}?bridgeEndpoint=true")

// In application.yml
rest:
  backend:
    url: ${REST_BACKEND_URL:localhost:8081}
```

Environment variable override pattern: `${ENV_VAR:default_value}`

### Conditional Backend Wiring

Central routes wire backends conditionally based on project generation configuration:
```java
// Only generated if REST backend with CRO operation was selected
.to("direct:createOrderRest").id("createOrderRestEndpoint")
```

This is resolved at project generation time, not at runtime.

### application.yml Configuration Blocks by Technology

When adding or modifying a technology, update `application.yml` with the appropriate configuration block.

**REST backend:**
```yaml
backend-purchase-rest-api:
  url: ${BACKEND_REST_URL:localhost:8091}
```










**Naming convention for topics/queues**: `productCatalogService-{direction}-{operation}-{type}`
- Direction: `northbound` (consumer) or `southbound` (producer)
- Type: `topic` (Kafka, MQTT), `queue` (AMQP, JMS, RabbitMQ, SQS), `topic-sns` (SNS)

---

## Content Type Constants

```java
public static final String APPLICATION_JSON = "application/json";
public static final String APPLICATION_XML = "application/xml";
public static final String APPLICATION_PROTOBUF = "application/protobuf";
public static final String APPLICATION_AVRO = "application/avro";
```

---

## Split Pattern (Batch Operations)

```java
// Split items from a domain object for per-item processing
.split(method(this, "extractItems"))
    .to("sql:{{sql.insert.item}}?dataSource=#{{vendorDataSource}}")
.end()

// Helper method (in route class)
public List<PurchaseItem> extractItems(Exchange exchange) {
    Purchase purchase = exchange.getMessage().getBody(Purchase.class);
    return purchase.getItems();
}
```

**Quarkus note**: Use `method(this, "extractItems")` instead of lambda — required for native compilation (avoids reflection).

---

## Aggregation Pattern (Batch Collection)

```java
// Aggregate individual results into a list
.aggregate(constant(true), new ArrayListAggregationStrategy())
    .completionSize(batchSize)
    .completionTimeout(5000)
    .to("direct:processBatch");
```

---

## Anti-Patterns

| Wrong | Right |
|-------|-------|
| `.marshal().json()` in Quarkus | `.marshal(jacksonDataFormat)` with injected ObjectMapper |
| `errorHandler(appErrorHandler())` in central/producer | `errorHandler(noErrorHandler())` — propagate to consumer |
| Missing `camelBeeRouteConfigurer.configureRoute(this)` | Always call first in `configure()` |
| Dead-letter for gRPC/WebSocket errors | Use `doTry/doCatch` for these protocols |
| `http://` in Camel endpoint URI | `http:` (no `://`) — Camel component syntax |
| Missing `bridgeEndpoint=true` | Required for all HTTP backend calls |
| Missing header cleanup before HTTP backend calls | Remove `HTTP_PATH`, `HTTP_URL` |
| Bean validation on central route | Validate on consumer (after unmarshal) only |
| Lambda in `.split()` for Quarkus native | Use `method(this, "methodName")` |
| Hard-coded URLs | Use `{{config.property}}` with env var fallback |
| `try/catch` in route code | Use `doTry/doCatch` (Camel DSL) or let dead-letter handle |
| Setting body directly for backend response | Use `Constants.ACTUAL_RESPONSE_BODY` property |
| Mixing format marshalers (JSON marshal for XML body) | Check `ORIGINAL_CONTENT_TYPE` / `ORIGINAL_ACCEPT_CONTENT_TYPE` |
| Binary format without `convertBodyTo(byte[].class)` | Proto and Avro unmarshal requires byte[] input |
