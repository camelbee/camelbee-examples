---
paths:
  - "src/main/java/**/routes/**"
  - "src/main/resources/application*.yml"
---
# Camel 4.x Standards

CamelBee-specific Apache Camel DSL patterns and conventions (**SPRINGBOOT**).

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
@Component
@RequiredArgsConstructor
public class MyConsumerRoute extends RouteBuilder {

    private final GenericExceptionHandler genericExceptionHandler;
    private final CamelBeeRouteConfigurer camelBeeRouteConfigurer;
    private final JsonOrderMapper jsonOrderMapper;

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
// Unmarshal
.unmarshal().json(JsonLibrary.Jackson, Order.class)
// Marshal
.marshal().json()
```




### Apache Avro

```java
// Unmarshal (requires byte[] conversion first)
.convertBodyTo(byte[].class)
.unmarshal().avro(AvroLibrary.ApacheAvro, Order.class)
// Marshal
.marshal().avro(AvroLibrary.ApacheAvro)
```




---

## Consumer Route Patterns by Protocol

### REST (OpenAPI-driven)

**OpenAPI → Route binding**: The OpenAPI `operationId` directly maps to a `direct:` endpoint name. To add a new REST operation, add it to `openapi/order-api.yaml` with an `operationId`, then create a matching `from("direct:{operationId}")` route.

| OpenAPI operationId | Route from() | Central route |
|---|---|---|
| `listOrders` | `from("direct:listOrders")` | `direct:centralListOrders` |
| `createOrder` | `from("direct:createOrder")` | `direct:centralCreateOrder` |
| `getOrder` | `from("direct:getOrder")` | `direct:centralGetOrder` |
| `replaceOrder` | `from("direct:replaceOrder")` | `direct:centralReplaceOrder` |
| `updateOrder` | `from("direct:updateOrder")` | `direct:centralUpdateOrder` |
| `deleteOrder` | `from("direct:deleteOrder")` | `direct:centralDeleteOrder` |
| `createOrdersBatch` | `from("direct:createOrdersBatch")` | `direct:centralCreateOrdersBatch` |

```java
// OpenAPI specification binding (endpoints auto-created from spec)
restConfiguration().bindingMode(RestBindingMode.off);
rest().openApi().specification("openapi/order-api.yaml").missingOperation("ignore");

// Each operation is routed from a direct: endpoint (named by OpenAPI operationId)
from("direct:createOrder")
    .routeId("createOrderOperationRoute")
    .process(this::setOriginalContentTypeProperties)
    // Single-format: unmarshal inline; multi-format: route to direct:unmarshalOrder
    .unmarshal().json(JsonLibrary.Jackson, Order.class)
    .to("bean-validator://camelbee")
    .convertBodyTo(Order.class)
    .to("direct:centralCreateOrder")
    .convertBodyTo(JsonOrder.class)
    .marshal().json()
    .process(this::setOriginalContentTypePropertiesBack)
    .setHeader(HTTP_RESPONSE_CODE, constant(201))
```

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
            .unmarshal().jaxb("io.fintech.loan.application.service.model.api.xml")
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

### Messaging Consumers (Kafka, AMQP, JMS, RabbitMQ, MQTT)

Common pattern for all messaging consumers:

```java
from("kafka:{{${serviceName}.northbound-createorder-topic}}-json")  // or amqp:queue:, jms:queue:, rabbitmq:, mqtt:
    .routeId("jsonKafkaCreateOrderConsumerRoute")
    .unmarshal().json(JsonLibrary.Jackson, Order.class)
    .convertBodyTo(Order.class)
    .to("direct:centralCreateOrder");
```

Messaging-specific endpoint options:

| Protocol | Key Options |
|----------|-------------|
| **Kafka** | `kafka:{{${serviceName}.northbound-{operation}-topic}}-{format}` |
| **AMQP** | `amqp:queue:{{${serviceName}.northbound-{operation}-queue}}-{format}?disableReplyTo=true&jmsMessageType=Text&transacted=true&receiveTimeout=30000` |
| **JMS** | `jms:queue:{{${serviceName}.northbound-{operation}-queue}}-{format}?disableReplyTo=true&jmsMessageType=Text&transacted=true&receiveTimeout=30000` |
| **RabbitMQ** | `spring-rabbitmq:${serviceName}-northbound?queues={{${serviceName}.northbound-{operation}-queue}}-{format}&routingKey=${serviceName}.{operation}.{format}` |
| **MQTT** | `paho-mqtt5:{{${serviceName}.northbound-{operation}-topic}}-{format}?brokerUrl={{${serviceName}.mqtt.broker-url}}&clientId=${serviceName}-{operation}-consumer` |
| **AWS SQS** | `aws2-sqs://{{${serviceName}.northbound-{operation}-queue}}-{format}` |

AMQP/JMS consumer example (same pattern, different component):
```java
from("amqp:queue:{{${serviceName}.northbound-createorder-queue}}-json" +
     "?disableReplyTo=true&jmsMessageType=Text&transacted=true&receiveTimeout=30000")
    .routeId("jsonAmqpCreateOrderConsumerRoute")
    .unmarshal().json(JsonLibrary.Jackson, Order.class)
    .convertBodyTo(Order.class)
    .to("direct:centralCreateOrder");
```

RabbitMQ consumer (uses exchange + routing key):
```java
from("spring-rabbitmq:${serviceName}-northbound" +
     "?queues={{${serviceName}.northbound-createorder-queue}}-json" +
     "&routingKey=${serviceName}.createorder.json")
    .routeId("jsonRabbitMqCreateOrderConsumerRoute")
    .unmarshal().json(JsonLibrary.Jackson, Order.class)
    .convertBodyTo(Order.class)
    .to("direct:centralCreateOrder");
```

MQTT consumer (topic-based, extracts transactionId from MQTT user properties):
```java
from("paho-mqtt5:{{${serviceName}.northbound-createorder-topic}}-json" +
     "?brokerUrl={{${serviceName}.mqtt.broker-url}}" +
     "&clientId=${serviceName}-createorder-consumer")
    .routeId("jsonMqttCreateOrderConsumerRoute")
    .process(e -> { /* extract transactionId from MQTT user properties */ })
    .unmarshal().json(JsonLibrary.Jackson, Order.class)
    .convertBodyTo(Order.class)
    .to("direct:centralCreateOrder");
```

AWS SQS consumer (uses OrderEvent wrapper — SQS doesn't reliably pass custom headers):
```java
from("aws2-sqs://{{${serviceName}.northbound-createorder-queue}}-json")
    .routeId("jsonAwsSqsCreateOrderConsumerRoute")
    .unmarshal().json(JsonLibrary.Jackson, OrderEvent.class)
    .process(e -> { /* extract transactionId from OrderEvent wrapper, set body to inner Order */ })
    .convertBodyTo(Order.class)
    .to("direct:centralCreateOrder");
```

Binary formats (Proto, Avro) on messaging:
```java
// Kafka: set deserializer for binary
from("kafka:{{${serviceName}.northbound-createorder-topic}}-proto" +
     "?valueDeserializer=org.apache.kafka.common.serialization.ByteArrayDeserializer" +
     "&keyDeserializer=org.apache.kafka.common.serialization.ByteArrayDeserializer")

// AMQP/JMS: use Bytes message type for binary
amqp:queue:{{queue}}-proto?jmsMessageType=Bytes  // (vs Text for JSON/XML)
```





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
    .marshal().json()
    .to("http:{{backend-purchase-rest-api.url}}?bridgeEndpoint=true")
    .unmarshal().json(JsonLibrary.Jackson, Purchase.class)
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
    .to("jpa:io.fintech.loan.application.service.model.infra.jpa.Purchase?entityManagerFactory=#{{jpaEntityManagerFactory}}")
        .id("createOrderJpaEndpoint");
```

Named queries for reads:
```java
.to("jpa:Purchase?namedQuery=Purchase.findPurchaseBySalesChannelAndId&singleResult=true")
```





### Messaging Backends (Kafka/AMQP/JMS/RabbitMQ/MQTT/AWS SQS/AWS SNS)

All messaging backends are **fire-and-forget** — they do not set `ACTUAL_RESPONSE_BODY`.

Kafka backend (already shown above in architecture examples).

AMQP backend (same pattern as JMS, different component):
```java
from("direct:createOrderAmqp").routeId("createOrderAmqpRoute")
    .errorHandler(noErrorHandler())
    .setBody(exchangeProperty(Constants.ORIGINAL_BODY))
    .removeHeaders("*")
    .convertBodyTo(Purchase.class)
    .marshal().json()
    .to("amqp:queue:{{${serviceName}.southbound-createorder-queue}}" +
        "?disableReplyTo=true&clientId=camelbee&jmsMessageType=Text");
```

JMS backend:
```java
from("direct:createOrderJms").routeId("createOrderJmsRoute")
    .errorHandler(noErrorHandler())
    .setBody(exchangeProperty(Constants.ORIGINAL_BODY))
    .removeHeaders("*")
    .convertBodyTo(Purchase.class)
    .marshal().json()
    .to("jms:queue:{{${serviceName}.southbound-createorder-queue}}" +
        "?disableReplyTo=true&clientId=camelbee&jmsMessageType=Text");
```

RabbitMQ backend (uses exchange + routing key):
```java
from("direct:createOrderRabbitMq").routeId("createOrderRabbitMqRoute")
    .errorHandler(noErrorHandler())
    .setBody(exchangeProperty(Constants.ORIGINAL_BODY))
    .removeHeaders("*")
    .setExchangePattern(ExchangePattern.InOnly)
    .convertBodyTo(Purchase.class)
    .marshal().json()
    .to("spring-rabbitmq:${serviceName}-southbound" +
        "?routingKey=${serviceName}.createorder.json&autoDeclareProducer=true");
```

MQTT backend (requires byte[] conversion):
```java
from("direct:createOrderMqtt").routeId("createOrderMqttRoute")
    .errorHandler(noErrorHandler())
    .setBody(exchangeProperty(Constants.ORIGINAL_BODY))
    .convertBodyTo(Purchase.class)
    .marshal().json()
    .convertBodyTo(byte[].class)  // MQTT requires byte[] payload
    .to("paho-mqtt5:{{${serviceName}.southbound-createorder-topic}}" +
        "?brokerUrl={{${serviceName}.mqtt.broker-url}}" +
        "&clientId=camelbee-createorder-producer");
```

AWS SQS backend (JSON and XML only, queue name has format suffix):
```java
from("direct:createOrderAwsSqs").routeId("createOrderAwsSqsRoute")
    .errorHandler(noErrorHandler())
    .setBody(exchangeProperty(Constants.ORIGINAL_BODY))
    .removeHeaders("*")
    .convertBodyTo(Purchase.class)
    .marshal().json()
    .to("aws2-sqs://{{${serviceName}.southbound-createorder-queue}}-json");
```

AWS SNS backend (topic-based, JSON and XML only):
```java
from("direct:createOrderAwsSns").routeId("createOrderAwsSnsRoute")
    .errorHandler(noErrorHandler())
    .setBody(exchangeProperty(Constants.ORIGINAL_BODY))
    .removeHeaders("*")
    .convertBodyTo(Purchase.class)
    .marshal().json()
    .to("aws2-sns://{{${serviceName}.southbound-createorder-topic-sns}}-json");
```

| Messaging Backend | Key Differences |
|---|---|
| **AMQP/JMS** | `jmsMessageType=Text` for JSON/XML, `Bytes` for Proto/Avro |
| **RabbitMQ** | `ExchangePattern.InOnly`, routing key includes operation + format |
| **MQTT** | Requires `.convertBodyTo(byte[].class)` before send, unique `clientId` per operation |
| **AWS SQS** | Queue name has format suffix (`-json`, `-xml`), JSON and XML only |
| **AWS SNS** | Topic name has format suffix (`-json`, `-xml`), JSON and XML only |



### Cache Backend (Write-Through)

Vendor selected at generation time via `backendsDetails` (position of CACHE): `REDIS`, `VALKEY`, `CAFFEINE`, or `INFINISPAN`. Values are serialized as JSON. Operations are limited to **writes** (CRO, CRB, REO, UPO, DEO) — LSO and GEO are intentionally NOT supported because read paths need cache-aside / read-through patterns that don't fit the fan-out architecture.

**Shared dispatch pattern** — vendor-specific `direct:cachePut` and `direct:cacheDelete` routes are called by every operation:

```java
// Vendor-specific PUT dispatch (generated once, reused by all write ops)
from("direct:cachePut").routeId("cachePutRoute")
    .to("spring-redis://{{${serviceName.toLowerCase()}.cache.host}}:{{${serviceName.toLowerCase()}.cache.port}}?redisTemplate=#stringRedisTemplate");

// Per-op route — set vendor headers, delegate to dispatch
from("direct:createOrderCache").routeId("createOrderCacheRoute")
    .setBody(exchangeProperty(Constants.ORIGINAL_BODY))
    .convertBodyTo(Purchase.class)
    .process(e -> {
        Purchase p = e.getIn().getBody(Purchase.class);
        ensureIds(p);
        e.setProperty("cachedPurchase", p);
        e.getIn().setHeader(RedisConstants.COMMAND, "SET");
        e.getIn().setHeader(RedisConstants.KEY, CACHE_KEY_PREFIX + p.getId());
        e.getIn().setHeader(RedisConstants.VALUE, serializePurchase(p));
    })
    .to("direct:cachePut")
    .process(e -> e.getIn().setBody(e.getProperty("cachedPurchase", Purchase.class)))
    .convertBodyTo(Order.class)
    .setProperty(Constants.ACTUAL_RESPONSE_BODY, body());
```

**Key conventions:**

| Element | Value |
|---|---|
| Cache key format | `${serviceName.toLowerCase()}:purchase:{id}` |
| Value format | JSON-serialized `Purchase` (via Jackson `ObjectMapper`) |
| Response body | Set `ACTUAL_RESPONSE_BODY` to the `Order` after cache write (PUT ops) |
| ID generation | `ensureIds(Purchase)` assigns a UUID if `id`/item `id` is missing |
| Batch (CRB) | `.split(body())` per-item, call `direct:cachePut` per item, aggregate at end |

**Vendor-specific headers / constants:**

| Vendor | Component | PUT | DELETE |
|---|---|---|---|
| Redis / Valkey | `spring-redis` | `RedisConstants.COMMAND=SET`, `KEY`, `VALUE` | `COMMAND=DEL`, `KEY` |
| Caffeine | `caffeine-cache` | `CaffeineConstants.ACTION=PUT`, `KEY`, body=value | `ACTION=INVALIDATE`, `KEY` |
| Infinispan | `infinispan` | `InfinispanConstants.KEY`, `VALUE`, `operation=PUT` | `KEY`, `operation=REMOVE` |

**Anti-patterns:**
- Adding LSO or GEO to the cache producer — cache reads need conditional routing (check cache, fall back on miss), which does not fit the central-route fan-out model
- Storing formats other than JSON — the CACHE backend implicitly uses JSON; `backendsDetails` selects the vendor, not the value format
- Calling the vendor endpoint inline per operation — use the shared `direct:cachePut` / `direct:cacheDelete` dispatch routes to keep vendor details in one place

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
| REST | Dead-letter channel + ResponseFormatter (auto) |
| Kafka/AMQP/JMS | Dead-letter channel + transaction rollback (auto) |

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

**Kafka (component + topics):**
```yaml
camel:
  component:
    kafka:
      brokers: ${KAFKA_URL:localhost:9092}
      groupId: "camelbeeService"
      autoOffsetReset: "earliest"

camelbeeService:
  northbound-createorder-topic: camelbeeService-northbound-createorder-topic
  southbound-createorder-topic: camelbeeService-southbound-createorder-topic
  # ... one per operation
```









**Cache backend:**
```yaml
camelbeeservice:
  cache:
    host: ${CACHE_HOST:localhost}
    port: ${CACHE_PORT:6379}
```
Vendor is fixed at generation time via `backendsDetails` (REDIS / VALKEY / CAFFEINE / INFINISPAN) — not a runtime switch.

**Naming convention for topics/queues**: `camelbeeService-{direction}-{operation}-{type}`
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
| Hard-coded URLs | Use `{{config.property}}` with env var fallback |
| `try/catch` in route code | Use `doTry/doCatch` (Camel DSL) or let dead-letter handle |
| Setting body directly for backend response | Use `Constants.ACTUAL_RESPONSE_BODY` property |
| Mixing format marshalers (JSON marshal for XML body) | Check `ORIGINAL_CONTENT_TYPE` / `ORIGINAL_ACCEPT_CONTENT_TYPE` |
| Binary format without `convertBodyTo(byte[].class)` | Proto and Avro unmarshal requires byte[] input |
| SOAP producer with `.to()` in SpringBoot tests | Use `.toD()` for registry bean reset in integration tests |
