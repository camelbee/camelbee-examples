---
paths:
  - "src/test/**"
  - "src/integration-test/**"
  - "src/black-box-test/**"
---
# Testing Standards

CamelBee testing conventions for **QUARKUS** microservices. Three test levels: Unit, Integration, Black-box.

---

## Quick Reference

| Level | Location | Framework | Runs With | Tests What |
|-------|----------|-----------|-----------|------------|
| **Unit** | `src/test/` | JUnit 5 + Camel Test | `./mvnw test` | Mappers + route logic (mocked backends) |
| **Integration** | `src/integration-test/` | JUnit 5 + TestContainers | `./mvnw jacoco:prepare-agent failsafe:integration-test failsafe:verify` | Full routes with real backends |
| **Black-box** | `src/black-box-test/` | JUnit 5 + Docker Compose | `./mvnw verify -Pblack-box-test` | Fully dockerized end-to-end |

---

## Test Naming Conventions

### Method Names

Pattern: `test_{Operation}_{Scenario}`

| Example | Description |
|---------|-------------|
| `test_CreateOrder_With_JsonFormat` | Happy path with JSON format |
| `test_CreateOrder_With_XmlFormat` | Happy path with XML format |
| `test_CreateOrder_ValidationError_NoItems` | Validation failure scenario |
| `test_CreateOrder_BackendError_Rest400` | Backend returns 400 |

For parameterized tests, the format parameter is passed via `@MethodSource`:
```java
@ParameterizedTest
@MethodSource("provideFormats")
void test_CreateOrder_With_Format(String format) { ... }
```

### Class Names

| Level | Pattern | Example |
|-------|---------|---------|
| Mapper unit test | `{Format}{Model}MapperTest` | `JsonOrderMapperTest`, `JsonPurchaseMapperTest` |
| Route unit test | `Central{Operation}RouteUnitTest` | `CentralCreateOrderRouteUnitTest` |
| Integration test | `{Operation}IntegrationTest` | `CreateOrderIntegrationTest` |
| Black-box test | `{Protocol}{Operation}BlackBoxTest` | `RestCreateOrderBlackBoxTest` |

### Annotations (Required)

| Annotation | When |
|------------|------|
| `@DisplayName` | Always on class and test methods |
| `@Test` | Single-scenario tests |
| `@ParameterizedTest` + `@MethodSource` | Multi-format / multi-backend tests |
| `@org.junit.jupiter.api.Order(N)` | When test execution order matters |

---

## Unit Tests

### Mapper Tests (`src/test/java/mapper/`)

Test bidirectional MapStruct mappings between model layers.

```java
class JsonOrderMapperTest {

    private JsonOrderMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(JsonOrderMapper.class);
    }

    @Test
    @DisplayName("Should map JSON Order to Domain Order")
    void test_JsonToDomain_Order() {
        // Given
        JsonOrder jsonOrder = new JsonOrder();
        jsonOrder.setId("123");
        jsonOrder.setSalesChannel("ONLINE");
        jsonOrder.setStatus(JsonOrder.StatusEnum.CONFIRMED);
        jsonOrder.setItems(List.of(createItem()));

        // When
        Order domainOrder = mapper.jsonToDomainOrder(jsonOrder);

        // Then
        assertNotNull(domainOrder);
        assertEquals("123", domainOrder.getId());
        assertEquals("ONLINE", domainOrder.getSalesChannel());
        assertEquals(StatusEnum.CONFIRMED, domainOrder.getStatus());
        assertEquals(1, domainOrder.getItems().size());
    }

    @Test
    @DisplayName("Should map Domain Order to JSON Order")
    void test_DomainToJson_Order() { ... }

    @Test
    @DisplayName("Should handle null input")
    void test_NullInput() {
        assertNull(mapper.jsonToDomainOrder(null));
    }

    @Test
    @DisplayName("Should map list of orders")
    void test_ListMapping() { ... }
}
```

**What to test per mapper**:

| Test | Description |
|------|-------------|
| Forward mapping | API -> Domain (or Domain -> Infra) |
| Reverse mapping | Domain -> API (or Infra -> Domain) |
| List mapping | `List<X>` to `List<Y>` |
| Null handling | `null` input returns `null` |
| Custom type conversions | `LocalDateTime` <-> `OffsetDateTime`, Status enums, XML wrapper types |
| Field name differences | `orderDate` <-> `purchaseDate` |

### Route Unit Tests (`src/test/java/routes/`)

Test route logic with all backends mocked via AdviceWith.

**Base class**:
```java
@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class UnitTest {
    @Inject FluentProducerTemplate fluentProducerTemplate;
    @Inject CamelContext camelContext;
}
```

**Mocking pattern** (AdviceWith + weaveById):
```java
@BeforeEach
void setUp() throws Exception {
    // Mock all backend endpoints from central route
    AdviceWith.adviceWith(camelContext, "centralCreateOrderRoute", a -> {
        a.weaveById("createOrderRestEndpoint").replace().to("mock:createOrderRest");
        a.weaveById("createOrderKafkaEndpoint").replace().to("mock:createOrderKafka");
        a.weaveById("createOrderAmqpEndpoint").replace().to("mock:createOrderAmqp");
        // ... one per enabled backend
    });
    camelContext.start();
}
```

**Sending test messages**:
```java
@Test
@DisplayName("Should route CreateOrder to all backends")
void test_CreateOrder_Success() throws Exception {
    // Given
    mockRestEndpoint.expectedMessageCount(1);
    mockKafkaEndpoint.expectedMessageCount(1);

    Order testOrder = createTestOrder();
    Exchange exchange = ExchangeBuilder.anExchange(camelContext).build();
    exchange.getMessage().setBody(testOrder);

    // When
    fluentProducerTemplate.to("direct:centralCreateOrder")
        .withExchange(exchange).send();

    // Then
    MockEndpoint.assertIsSatisfied(camelContext);
}
```

**Test data producers**:
```java
// Domain-level test data (shared across unit tests)
List<RequestResponseScenario> scenarios =
    CreateOrderDomainTestDataProducer.generateCreateOrderRequests();

Order order = getOrderByScenarioName(scenarios, RequestScenarios.CREATE_ORDER_SUCCESS);
```

---

## Integration Tests

### Base Class (`IntegrationTest.java`)

```java
@QuarkusTest
@TestProfile(IntegrationTestProfile.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Import({TestContainerConfiguration.class})
public abstract class IntegrationTest {
    @Inject CamelContext camelContext;
    @Inject FluentProducerTemplate fluentProducerTemplate;
}
```

**Key features**:
- `TestContainerConfiguration` starts Docker backends (WireMock, databases, brokers)
- Static `initialized` flag prevents duplicate route setup
- WireMock on port 8091 for REST/SOAP/gRPC backend mocking
- AdviceWith on `errorHandlerRoute` to capture errors: `a.weaveAddFirst().to("mock:error")`
- **Mock capture endpoints on every producer route** to verify backend calls and responses

### Integration Test Mock Capture Pattern (CRITICAL)

Each integration test class appends a `mock:capture{Operation}{Backend}` endpoint to the **end** of every producer route using `weaveAddLast`. This allows tests to:
- **Verify the backend was called** (or not called in error scenarios)
- **Inspect the response** returned from the backend
- **Assert the data transformation** (domain model after round-trip through producer)

```java
// In setup() — append mock endpoint to the end of each producer route
AdviceWith.adviceWith(camelContext, "createOrderRestRoute",
    a -> a.weaveAddLast().to("mock:captureCreateOrderRest"));
AdviceWith.adviceWith(camelContext, "createOrderKafkaRoute",
    a -> a.weaveAddLast().to("mock:captureCreateOrderKafka"));
// ... one for each backend that supports this operation

// Inject the mock endpoints
@EndpointInject("mock:captureCreateOrderRest")
protected MockEndpoint captureCreateOrderRest;
```

**Two mock endpoint lists** manage test lifecycle:

```java
// captureMockEndpoints — reset before each test to clear previous results
captureMockEndpoints = Arrays.asList(captureError, captureCreateOrderRest, captureCreateOrderKafka, ...);

// verifyMockEndpoints — verify assertions after each test
verifyMockEndpoints = Arrays.asList(captureCreateOrderRest, captureCreateOrderKafka, ...);

// Before each test:
captureMockEndpoints.forEach(endpoint -> { endpoint.reset(); endpoint.expectedMessageCount(0); });

// After each test:
verifyMockEndpoints.forEach(endpoint -> { MockEndpoint.assertIsSatisfied(camelContext, 10, TimeUnit.SECONDS); });
```

**Verification pattern in tests:**

```java
// Happy path: expect backend was called and returned correct data
captureCreateOrderRest.expectedMessageCount(1);
// ... trigger the test ...
Order result = (Order) captureCreateOrderRest.getReceivedExchanges().get(0).getIn().getBody();
assertThat(result.getId()).isNotNull();

// Error path: expect backend was NOT called
captureCreateOrderRest.expectedMessageCount(0);
```

### Backend Data Verification via Camel Components

Integration tests use **Camel's own components** (via `FluentProducerTemplate`) to directly query backends and verify data was persisted or sent correctly. This is set up in the base `IntegrationTest` class:

```java
// SQL/JPA: query database directly via Camel SQL component
fluentProducerTemplate.to("direct:queryDatabaseSql")
    .withBody("SELECT COUNT(*) FROM purchases").request();

// Cassandra: query via Camel CQL component
fluentProducerTemplate.to("direct:resetCassandra")
    .withHeader("cql", "SELECT * FROM camelbee_purchases_table").request();

// MongoDB: query via Camel MongoDB component
fluentProducerTemplate.to("direct:resetMongodbFind")
    .withHeader("collection", "camelbee_purchases").request();

// Kafka: consume messages from southbound topic
fluentProducerTemplate.to("kafka:productCatalogService.southbound-createorder-topic-json"
    + "?groupId=test&autoOffsetReset=earliest").request();

// AMQP/JMS: consume messages from southbound queue
fluentProducerTemplate.to("amqp:queue:productCatalogService.southbound-createorder-queue-json").request();

// File/S3: check output directory for written files
assertThat(new File("outputdir/purchase/create/json/").listFiles()).hasLength(1);
```

These verification routes are created dynamically in `IntegrationTest.setup()` using `modelContext.addRouteDefinition()`. **When adding new operations, you MUST add corresponding verification routes and assertions.**

### IMPORTANT: Read Existing Tests Before Writing New Ones

The generated integration tests contain the complete working pattern for every interface and backend combination. **Before writing a new integration test:**

1. Read the existing `{Operation}IntegrationTest.java` base class for the operation most similar to yours
2. Read the interface-specific test (e.g., `RestInterfaceCreateOrderIntegrationTest.java`) for how it calls the route
3. Read `IntegrationTest.java` base class to understand the mock capture setup and verification routes
4. Follow the exact same structure: mock endpoint injection → setup() with weaveAddLast → test with expectedMessageCount → verify with getReceivedExchanges

### Parameterized Multi-Format Tests

```java
@ParameterizedTest
@MethodSource("provideFormats")
@DisplayName("Should create order via REST in multiple formats")
void test_CreateOrder_With_Format(String format) {
    // Given
    String requestBody = readResource(
        CREATEORDER_BASE_PATH_API.formatted(format) + "-success-request." + extension(format));

    // When
    var response = RestAssured.given()
        .contentType(contentTypeFor(format))
        .accept(contentTypeFor(format))
        .body(requestBody)
        .post("/api/orders");

    // Then
    response.then().statusCode(201);

    // Verify backend received correct infra model
    wireMock.verify(1, postRequestedFor(urlEqualTo("/purchases"))
        .withRequestBody(equalToJson(
            readResource(CREATEORDER_BASE_PATH_INFRA.formatted("json") + "-success-response.json")
        )));
}

static Stream<Arguments> provideFormats() {
    return Stream.of(
    );
}
```

### WireMock Verification Patterns

```java
// JSON body verification
wireMock.verify(1, postRequestedFor(urlEqualTo("/purchases"))
    .withRequestBody(equalToJson(expectedJson)));

// XML body verification
wireMock.verify(1, postRequestedFor(urlEqualTo("/purchases"))
    .withRequestBody(equalToXml(expectedXml)));

// Binary body verification (Proto/Avro)
wireMock.verify(1, postRequestedFor(urlEqualTo("/purchases"))
    .withRequestBody(binaryEqualTo(expectedBytes)));

// No calls verification (error scenarios)
wireMock.verify(0, postRequestedFor(urlEqualTo("/purchases")));
```

### Backend Verification (Beyond WireMock)

```java
// Database verification
assertThat(dataVerifier.countJpaPurchases()).isEqualTo(1);


```

### Test Data Resources

```
src/integration-test/resources/data/inttest/
+-- api/                         (request payloads per format)
|   +-- json/
|   |   +-- createorder/
|   |   |   +-- createorder-success-request.json
|   |   |   +-- createorder-backend-error-rest-400-request.json
|   |   +-- listorders/
|   |   +-- getorder/
|   |   +-- ...
|   +-- xml/
|   +-- proto/
|   +-- avro/
+-- infra/                       (expected backend payloads per format)
    +-- json/
    |   +-- createpurchase/
    |   |   +-- createpurchase-success-response.json
    +-- xml/
    +-- proto/
    +-- soap/
```

#### Naming Convention for Messaging Topics/Queues

- **Interface (consumer / northbound):** `productCatalogService.northbound-{operation}-topic` or `productCatalogService.northbound-{operation}-queue` -- messages coming IN to the microservice
- **Backend (producer / southbound):** `productCatalogService.southbound-{operation}-topic` or `productCatalogService.southbound-{operation}-queue` -- messages going OUT from the microservice

This convention applies to Kafka topics, AMQP queues, JMS queues, RabbitMQ exchanges, MQTT topics, AWS SQS queues, and AWS SNS topics. The format suffix (e.g., `-json`, `-xml`) is appended to the topic/queue name.

### Test Data Producers

Three levels of test data generation:

| Level | Location | Class Pattern | Purpose |
|-------|----------|---------------|---------|
| **Domain** | `src/test/java/utils/testdata/` | `{Operation}DomainTestDataProducer` | Generates domain `Order` objects with named scenarios |
| **API** | `src/integration-test/java/utils/testdata/` | `{Format}OrderDataProducerApi` | Converts domain -> format-specific API models |
| **Infra** | `src/integration-test/java/utils/testdata/` | `{Format}PurchaseDataProducerInfra` | Converts domain -> format-specific backend models |

**Domain producers** (`src/test/java/utils/testdata/`):

| Class | Used By |
|-------|---------|
| `BaseDomainTestDataProducer` | Shared constants and helper methods (transaction IDs, scenario names) |
| `CreateOrderDomainTestDataProducer` | CreateOrder scenarios |
| `ListOrdersDomainTestDataProducer` | ListOrders scenarios |
| `GetOrderDomainTestDataProducer` | GetOrder scenarios |
| `ReplaceOrderDomainTestDataProducer` | ReplaceOrder scenarios |
| `UpdateOrderDomainTestDataProducer` | UpdateOrder scenarios |
| `DeleteOrderDomainTestDataProducer` | DeleteOrder scenarios |
| `CreateOrdersBatchDomainTestDataProducer` | Batch scenarios |
| `RequestResponseScenario` | Data class holding scenario name, order, error, pagination |

**API and Infra producers** (`src/integration-test/java/utils/testdata/`):

| Class | Format | Generates |
|-------|--------|-----------|
| `JsonOrderDataProducerApi` | JSON | API request/response JSON files |
| `XmlOrderDataProducerApi` | XML | API request/response XML files |
| `ProtoOrderDataProducerApi` | Protobuf | **Binary** `.bin` files from proto models |
| `AvroOrderDataProducerApi` | Avro | **Binary** `.avro` files from Avro models |
| `SoapOrderDataProducerApi` | SOAP | SOAP envelope XML files |
| `GrpcOrderDataProducerApi` | gRPC | gRPC proto request binary files |
| `GraphqlOrderDataProducerApi` | GraphQL | GraphQL query JSON files |
| `McpOrderDataProducerApi` | MCP | MCP tool call JSON files |
| `CsvDataProducer` | CSV | CSV files from JSON/XML source |
| `JsonPurchaseDataProducerInfra` | JSON | Backend purchase JSON files |
| `XmlPurchaseDataProducerInfra` | XML | Backend purchase XML files |
| `ProtoPurchaseDataProducerInfra` | Protobuf | **Binary** backend purchase `.bin` files |
| `AvroPurchaseDataProducerInfra` | Avro | **Binary** backend purchase `.avro` files |
| `SoapPurchaseDataProducerInfra` | SOAP | Backend SOAP envelope files |
| `GrpcPurchaseDataProducerInfra` | gRPC | Backend gRPC proto binary files |

#### Why Binary Test Data Producers Are Critical

**JSON and XML** test data files can be created manually or edited as text. But **Proto and Avro** formats are **binary** — you cannot create or edit them by hand. The data producer classes are the **only way** to generate these binary test fixtures.

**When tests verify binary backends** (e.g., WireMock `binaryEqualTo()` for Proto/Avro request matching, or Kafka message comparison), the expected binary data **must** be generated by these producer classes. Without them, you cannot write assertions for binary format tests.

**`MultiFormatTestDataGenerator`** is the entry point that runs all enabled data producers:
```java
public class MultiFormatTestDataGenerator {
    public static void main(String[] args) {
        // Generates test data for all enabled formats
        new JsonOrderDataProducerApi().generateAllFiles();
        new ProtoOrderDataProducerApi().generateAllFiles();    // binary
        new AvroOrderDataProducerApi().generateAllFiles();     // binary
        new JsonPurchaseDataProducerInfra().generateAllFiles();
        new ProtoPurchaseDataProducerInfra().generateAllFiles(); // binary
        new AvroPurchaseDataProducerInfra().generateAllFiles();  // binary
        // ... all enabled formats
    }
}
```

**Data flow**: Domain producer creates `Order` objects → API/Infra producer converts using MapStruct mappers → serializes to the target format → writes to `data/inttest/` directory.

**When to regenerate**: Run `MultiFormatTestDataGenerator.main()` after:
- Changing the domain model (Order, OrderItem fields)
- Changing MapStruct mapper logic
- Adding new test scenarios to domain producers
- Adding new operations

```java
// RequestResponseScenario structure
@Data @Builder
class RequestResponseScenario {
    String name;           // e.g., "CREATE_ORDER_SUCCESS"
    Order order;           // domain model
    List<Order> orders;    // for batch operations
    Error error;           // expected error (for error scenarios)
    Integer page;          // pagination
    Integer pageSize;
    String transactionId;  // correlation
}
```

---

## Black-Box Tests

### Base Class (`BlackBoxTest.java`)

```java
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class BlackBoxTest {

    // Manual mode: skip TestContainers, connect to running app
    private static final boolean MANUAL_MODE = Boolean.getBoolean("bbtest.manual");

    static {
        if (!MANUAL_MODE) {
            var ignored = TestContainerConfiguration.ENV;  // triggers Docker Compose
        }
    }

    // Helpers
    protected String readResource(String path) { ... }
    protected byte[] readResourceBinary(String path) { ... }
    protected String obtainAccessToken(String clientId, String clientSecret) { ... }
}
```

### Running Modes

| Mode | Command | Use Case |
|------|---------|----------|
| Automatic | `./mvnw verify -Pblack-box-test` | CI/CD pipeline |
| Manual | `./mvnw verify -Pblack-box-test -Dbbtest.manual=true -Dbbtest.host=localhost -Dbbtest.port=8080` | Local debugging |
| Native | `./mvnw verify -Pblack-box-test -Dbbtest.native=true` | Quarkus native image validation |

### Protocol-Specific Black-Box Tests

Each protocol has its own test class that extends `BlackBoxTest`:

| Protocol | Test Directory | Client Library |
|----------|---------------|----------------|
| MCP | `bbtest/mcp/` | HTTP client |

### Black-Box Test Structure

```java
class RestCreateOrderBlackBoxTest extends BlackBoxTest {

    @Test
    @Order(1)
    @DisplayName("Health check")
    void test_HealthCheck() {
        RestAssured.given()
            .get("/q/health")
            .then().statusCode(200);
    }

    @Test
    @Order(2)
    @DisplayName("Create order - JSON success")
    void test_CreateOrder_Json_Success() {
        // Setup: reset WireMock, clear databases, purge queues
        setupCreateOrderSuccessScenario("createorder-success-request.json");

        // Act: send request
        var response = RestAssured.given()
            .contentType("application/json")
            .body(readResource("data/bbtest/api/json/createorder/createorder-success-request.json"))
            .post("/api/orders");

        // Assert: response
        response.then().statusCode(201);

        // Assert: verify all backends received correct data
        validateCreateOrderSuccessScenario("createorder-success-request.json");
    }

    @Test
    @Order(3)
    @DisplayName("Create order - validation error")
    void test_CreateOrder_ValidationError() {
        // Act
        var response = RestAssured.given()
            .contentType("application/json")
            .body("{\"salesChannel\":\"ONLINE\",\"items\":[]}")
            .post("/api/orders");

        // Assert
        response.then().statusCode(400);
        validateCreateOrderErrorScenario();  // no backends called
    }
}
```

### Setup/Teardown Helpers

```java
// Setup: clean state before each test
void setupCreateOrderSuccessScenario(String fileName) {
    wireMock.resetAll();
    // Stub REST backend success response
    wireMock.register(post(urlEqualTo("/purchases"))
        .willReturn(aResponse()
            .withStatus(201)
            .withHeader("Content-Type", "application/json")
            .withBody(readResource("data/bbtest/infra/json/createpurchase/" + fileName))));

    dataVerifier.clearSqlPurchases();
    dataVerifier.clearJpaPurchases();
    messageVerifier.purgeKafkaTopics();
    messageVerifier.purgeAmqpQueues();
}

// Validate: verify all backends received data
void validateCreateOrderSuccessScenario(String fileName) {
    // REST backend
    wireMock.verify(1, postRequestedFor(urlEqualTo("/purchases")));

    // Database backends
    assertThat(dataVerifier.countSqlPurchases()).isEqualTo(1);

    // Message backends
    assertThat(messageVerifier.consumeKafkaMessages("topic-json", 1, 5000)).hasSize(1);

    // File backend
    Awaitility.await().atMost(Duration.ofSeconds(10))
        .until(() -> new File("outputdir/purchase/create/json/").listFiles().length > 0);
}
```

### Error Scenario Testing (Transaction ID Triggers)

Black-box tests use predefined transaction IDs to trigger specific WireMock error stubs:

```java
// Constants in BlackBoxTest
static final String TX_ID_REST_400 = "bbtest-rest-error-400";
static final String TX_ID_REST_404 = "bbtest-rest-error-404";
static final String TX_ID_REST_500 = "bbtest-rest-error-500";

// WireMock stub: match by transactionId header
wireMock.register(post(urlEqualTo("/purchases"))
    .withHeader("X-Transaction-Id", equalTo(TX_ID_REST_400))
    .willReturn(aResponse().withStatus(400)
        .withBody("{\"code\":\"BAD_REQUEST\"}")));
```

---

## Assertion Library Preferences

| Library | Use For |
|---------|---------|
| **AssertJ** (preferred) | Object/collection/string assertions: `assertThat(x).isEqualTo(y)` |
| **JUnit Assertions** | Simple equality: `assertEquals()`, `assertNotNull()` |
| **WireMock verify** | HTTP backend call verification |
| **MockEndpoint** | Camel route endpoint verification |
| **Awaitility** | Async assertions (file polling, message consumption): `Awaitility.await().atMost(...)` |

---

## Test Fixture Rules

| Rule | Description |
|------|-------------|
| **Never modify existing test fixtures** | Create new fixtures for new scenarios |
| **Scenario-based data** | Use `RequestResponseScenario` with named constants |
| **Format-specific files** | Separate data files per format (json/, xml/, proto/, avro/) |
| **Deterministic IDs** | Use fixed IDs in test data, not random |
| **Clean state** | Each test must clean up (clearDB, purgeQueues, resetWireMock) before running |

---

## Docker Compose for Tests

### Integration Tests

```yaml
# src/integration-test/resources/compose-backends.yml
# Started by TestContainerConfiguration
services:
  wiremock:          # REST/SOAP/gRPC backend mock (port 8091)
  postgresql:        # SQL/JPA database (port 5432)
```

#### Docker Compose Service Patterns

When adding a new backend service, follow these patterns in `compose-backends.yml`:

**Database service** (with healthcheck + Flyway migration):
```yaml
  postgresql:
    image: postgres:14-alpine
    ports: ["5432:5432"]
    environment:
      POSTGRES_USER: camelbee_user
      POSTGRES_PASSWORD: secret
      POSTGRES_DB: CAMELBEE_DATABASE
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U camelbee_user -d CAMELBEE_DATABASE"]
      interval: 10s
      timeout: 5s
      retries: 5
  flyway-postgresql:
    image: flyway/flyway
    command: -url=jdbc:postgresql://postgresql:5432/CAMELBEE_DATABASE -user=camelbee_user -password=secret migrate
    volumes: ["./docker/flyway/postgresql:/flyway/sql"]
    depends_on:
      postgresql: { condition: service_healthy }
```


#### Integration Test Resources Reference

All integration test resources are under `src/integration-test/resources/`:

| Directory | Purpose | Key Files |
|-----------|---------|-----------|
| `compose-backends.yml` | Docker Compose for TestContainers | Started by `TestContainerConfiguration` |
| `data/inttest/api/{format}/{operation}/` | API-level test data (request payloads) | Generated by `{Format}OrderDataProducerApi` |
| `data/inttest/infra/{format}/{operation}/` | Backend-level expected data (response payloads) | Generated by `{Format}PurchaseDataProducerInfra` |
| `docker/wiremock/mappings/{backend}/{operation}/` | WireMock request stubs (success + error scenarios) | JSON mapping files matching URL + headers |
| `docker/wiremock/__files/{backend}/{format}/{operation}/` | WireMock response bodies | `.json`, `.xml`, `.pb`, `.avro` files |
| `docker/jdbc/{vendor}/` | Flyway SQL migration scripts | `V1__Create_User.sql`, `V2__Create_Tables.sql`, `R__Verify_Ready.sql` |
| `backend/sql/reset-{vendor}.sql` | SQL reset script (truncate + seed data) | Executed by `resetPersistenceLayers()` before each test |

**When adding new operations:** add test data files in both `data/inttest/api/` (request payloads) and `data/inttest/infra/` (expected backend payloads) following the existing naming pattern: `{operation}-success-request.{ext}`, `{operation}-backend-error-rest-400-request.{ext}`, etc.

**When adding new WireMock stubs:** add mapping files in `docker/wiremock/mappings/{backend}/{operation}/` and response bodies in `docker/wiremock/__files/{backend}/{format}/{operation}/`. Follow the existing naming pattern for success and error stubs.

### Black-Box Tests

```yaml
# src/black-box-test/resources/compose-blackbox.yml
# Includes compose-backends.yml PLUS the application itself
services:
  app:
    build: .
    ports: ["8080:8080", "8199:8199", "8299:8299"]
    depends_on: [wiremock, ...]
    environment:
      JAVA_OPTS: "-XX:MetaspaceSize=512m -XX:MaxMetaspaceSize=1024m"
      BACKEND_REST_URL: http://wiremock:8080
      # ... all backend connection URLs use Docker service names
```

**Key rule**: In Docker Compose, backend URLs use **Docker service names** (e.g., `broker:29092`) not `localhost`.

---

## Integration Test Patterns by Technology

Each interface technology has its own test class that extends a shared base (e.g., `CreateOrderIntegrationTest`). The test creates a **dynamic route** in `@BeforeAll` that sends messages through the interface.


---

## Anti-Patterns

| Wrong | Right |
|-------|-------|
| Testing mapper + route in same test | Separate mapper tests (unit) from route tests |
| Hard-coded port numbers in tests | Use `@LocalServerPort` or TestContainers dynamic ports |
| Sharing state between tests | Clean state in setup, use `@TestInstance(PER_CLASS)` + `@Order` |
| Testing only happy path | Include validation errors, backend errors, format-specific edge cases |
| Mocking with `mock:` without `weaveById` | Always use `AdviceWith` + `weaveById("endpointId")` for targeted mocking |
| Skipping binary format tests | Test Proto and Avro alongside JSON/XML |
| Asserting only response code | Verify backend calls (WireMock), database state, message queues |
| Random test data | Use deterministic `RequestResponseScenario` producers |
| Modifying existing test fixtures | Create new fixtures; existing ones may be used by other tests |
| Missing `@DisplayName` | Always add for readable test reports |
| Polling without Awaitility | Use `Awaitility.await().atMost(...)` for async assertions |
