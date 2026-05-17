package io.fintech.loan.application.service.itest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer;
import com.github.tomakehurst.wiremock.client.WireMock;
import jakarta.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.FluentProducerTemplate;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.model.RouteDefinition;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration Tester with TestContainers.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"itest"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Import({TestContainerConfiguration.class})
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public abstract class IntegrationTest {

  protected static final String BASE_PATH_API = "/data/inttest/api/";
  protected static final String BASE_PATH_INFRA = "/data/inttest/infra/";

  @Inject
  protected CamelContext camelContext;

  @Inject
  protected FluentProducerTemplate fluentProducerTemplate;

  @EndpointInject("mock:error")
  protected MockEndpoint captureError;

  protected List<MockEndpoint> captureMockEndpoints;
  protected List<MockEndpoint> verifyMockEndpoints;

  protected static WireMock wireMock;

  static {

    WireMock.configureFor("localhost", 8091);

    wireMock = new WireMock("localhost", 8091);

  }

  private static boolean initialized = false;

  protected static final ObjectMapper objectMapper = createObjectMapper();

  public void setup() throws Exception {

    /*
     * We use a flag to ensure routes are create or advised only once across all test classes
     * that extend this base class. A static initialization block cannot be used here
     * because it would run before Spring injects the camelContext, making it
     * unavailable during class loading.
     */
    if (initialized) {
      return;
    }

    initialized = true;

    // add a new mock endpoint to the beginning of errorHandlerRoute to capture the errors
    AdviceWith.adviceWith(camelContext, "errorHandlerRoute",
        a -> a.weaveAddFirst().to("mock:error"));

    var modelContext = camelContext.getCamelContextExtension().getContextPlugin(ModelCamelContext.class);

    var routeJpaPurchaseSouthbound = new RouteDefinition();
    routeJpaPurchaseSouthbound.from("direct:queryDatabaseJpa")
        .toD("jpa:io.fintech.loan.application.service.model.infra.jpa.postgresql.Purchase?query=${body}");

    modelContext.addRouteDefinition(routeJpaPurchaseSouthbound);

    var routeJdbcPurchaseSouthbound = new RouteDefinition();

    routeJdbcPurchaseSouthbound.from("direct:queryDatabaseJdbc")
        .to("jdbc:default?useHeadersAsParameters=true");
    modelContext.addRouteDefinition(routeJdbcPurchaseSouthbound);

    // Route that fetches a value from the cache by key — used by tests to verify write-through.
    var routeCacheGet = new RouteDefinition();
    routeCacheGet.from("direct:queryCache")
        .toD("spring-redis://{{camelbeeservice.cache.host}}:{{camelbeeservice.cache.port}}?command=GET&redisTemplate=#stringRedisTemplate");
    modelContext.addRouteDefinition(routeCacheGet);

    // Route that wipes the cache — used by tests to guarantee a clean state between scenarios.
    var routeCacheClear = new RouteDefinition();
    routeCacheClear.from("direct:clearCache")
        .toD("spring-redis://{{camelbeeservice.cache.host}}:{{camelbeeservice.cache.port}}?command=FLUSHDB&redisTemplate=#stringRedisTemplate");
    modelContext.addRouteDefinition(routeCacheClear);
  }

  protected void resetAllMockedEndpoints() {
    // Reset all endpoints and set wait time
    captureMockEndpoints.forEach(endpoint -> {
      endpoint.reset();
      endpoint.setResultWaitTime(100);
    });

    verifyMockEndpoints.forEach(endpoint -> {
      endpoint.reset();
      endpoint.setResultWaitTime(5000);
    });
  }

  protected void resetBeforeAll() throws Exception {

    resetPersistenceLayers("reset-postgresql.sql", "postgresqlDataSource", true);

  }

  private void resetPersistenceLayers(String fileName, String dataSource, boolean isMultiLinesSupported) throws Exception {

    // Remove comments (block, line, and hash-based)
    String cleanedSql = readResource("/backend/sql/%s".formatted(fileName))
        .replaceAll("/\\*.*?\\*/", "")  // Remove block comments /* ... */
        .replaceAll("--.*", "")        // Remove single-line comments --
        .replaceAll("#.*", "");        // Remove single-line comments #

    if (isMultiLinesSupported) {

      var result = fluentProducerTemplate.to("direct:queryDatabaseJdbc")
          .withBody(cleanedSql).withHeader("dataSourceName", dataSource).request();

    } else {
      // Split SQL statements to execute them individually
      for (String statement : cleanedSql.split(";")) {
        if (!statement.trim().isEmpty()) {
          var result = fluentProducerTemplate.to("direct:queryDatabaseJdbc")
              .withBody(statement.trim()).withHeader("dataSourceName", dataSource).request();

        }
      }
    }

  }

  protected void clearJpaTables() throws Exception {
    fluentProducerTemplate.to("direct:queryDatabaseJpa")
        .withBody("DELETE FROM PurchaseItem purchaseItem")
        .request();
    fluentProducerTemplate.to("direct:queryDatabaseJpa")
        .withBody("DELETE FROM Purchase purchase")
        .request();
  }

  @SuppressWarnings("unchecked")
  protected List queryJpaPurchases() throws Exception {
    return (List) fluentProducerTemplate.to("direct:queryDatabaseJpa")
        .withBody("select purchase from Purchase purchase")
        .request();
  }

  @SuppressWarnings("unchecked")
  protected List queryJpaPurchases(String whereClause) throws Exception {
    return (List) fluentProducerTemplate.to("direct:queryDatabaseJpa")
        .withBody("select purchase from Purchase purchase WHERE " + whereClause)
        .request();
  }

  protected void clearCacheTables() {
    fluentProducerTemplate.to("direct:clearCache").withBody(null).request();
  }

  private static final String CACHE_KEY_PREFIX_ITEST = "camelbeeservice:purchase:";

  /**
   * Fetches the cached JSON Purchase for a given purchase id. Returns null if absent
   * (e.g., after DEO removed it). Used by integration tests to verify write-through semantics.
   */
  protected String getCachedPurchaseJson(String purchaseId) {
    return (String) fluentProducerTemplate.to("direct:queryCache")
        .withHeader(org.apache.camel.component.redis.RedisConstants.KEY, CACHE_KEY_PREFIX_ITEST + purchaseId)
        .withBody(null).request(String.class);
  }

  protected String readResource(String path) throws Exception {
    return IOUtils.resourceToString(path, StandardCharsets.UTF_8);
  }

  protected byte[] readResourceBinary(String path) throws Exception {
    return IOUtils.resourceToByteArray(path);
  }

  protected void setBody(Exchange exchange, String payloadFormat, String requestFile) throws Exception {
    if (isBinaryFormat(payloadFormat)) {
      byte[] data = readResourceBinary(requestFile);
      exchange.getIn().setBody(data);
      exchange.getIn().setHeader("Content-Length", data.length);
    } else {
      exchange.getIn().setBody(readResource(requestFile));
    }
  }

  protected boolean isBinaryFormat(String payloadFormat) {
    return Set.of("avro", "proto").contains(payloadFormat);
  }

  /**
   * Reads a fixture file and decodes it into the typed model class the Schema
   * Registry-aware Kafka serializer/deserializer works with. The Confluent
   * KafkaAvroSerializer / KafkaProtobufSerializer / KafkaJsonSchemaSerializer all
   * exchange typed in-memory objects, not the marshaled bytes that the plain
   * ByteArraySerializer carries — so both the test producer side (encode before
   * send) and the verifier mock side (compare against the auto-deserialized
   * incoming body) need this conversion. Returns the raw string for "xml" since
   * SR does not support XML and those tests fall back to the non-SR path.
   */
  protected Object readBodyForKafkaSchemaRegistry(String payloadFormat,
      String requestFile, Class<?> targetType) throws Exception {
    switch (payloadFormat.toLowerCase()) {
      case "json" -> {
        return objectMapper.readValue(readResource(requestFile), targetType);
      }
      case "proto" -> {
        // proto generated classes expose static parseFrom(byte[]) — invoke reflectively
        // since the target type is operation-specific (Order vs. Orders).
        return targetType.getMethod("parseFrom", byte[].class)
            .invoke(null, (Object) readResourceBinary(requestFile));
      }
      case "avro" -> {
        var schema = (org.apache.avro.Schema) targetType.getMethod("getClassSchema").invoke(null);
        var datumReader = new org.apache.avro.specific.SpecificDatumReader<>(schema);
        var decoder = org.apache.avro.io.DecoderFactory.get()
            .binaryDecoder(readResourceBinary(requestFile), null);
        return datumReader.read(null, decoder);
      }
      default -> {
        // xml falls through — the calling test routes back to the non-SR path
        return readResource(requestFile);
      }
    }
  }

  /**
   * Producer-side variant: decodes the fixture and sets it on the exchange so the
   * SR-aware serializer can encode it on send.
   */
  protected Object setBodyForKafkaSchemaRegistry(Exchange exchange, String payloadFormat,
      String requestFile, Class<?> targetType) throws Exception {
    Object body = readBodyForKafkaSchemaRegistry(payloadFormat, requestFile, targetType);
    exchange.getIn().setBody(body);
    return body;
  }

  protected String getFilePostFix(String payloadFormat) {
    if (payloadFormat == null || payloadFormat.isBlank()) {
      throw new IllegalArgumentException("payloadFormat must not be null or blank");
    }

    return switch (payloadFormat.toLowerCase()) {
      case "json" -> "json";
      case "xml" -> "xml";
      case "proto" -> "pb";
      case "avro" -> "avro";
      default -> throw new IllegalArgumentException(
          "Unsupported payload format: " + payloadFormat);
    };
  }

  private static ObjectMapper createObjectMapper() {
    ObjectMapper mapper = new ObjectMapper();

    JavaTimeModule javaTimeModule = new JavaTimeModule();
    javaTimeModule.addSerializer(
        java.time.LocalDate.class,
        new LocalDateSerializer(DateTimeFormatter.ISO_DATE)
    );
    javaTimeModule.addSerializer(
        java.time.OffsetDateTime.class,
        OffsetDateTimeSerializer.INSTANCE
    );

    mapper.registerModule(javaTimeModule);
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    mapper.enable(SerializationFeature.INDENT_OUTPUT);
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

    return mapper;
  }

  protected ObjectMapper getObjectMapper() {
    return objectMapper;
  }

}
