package com.mycompany.catalog.mcp.itest;

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
import org.apache.camel.quarkus.test.CamelQuarkusTestSupport;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Integration Tester with TestContainers.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(Lifecycle.PER_CLASS)
public abstract class IntegrationTest extends CamelQuarkusTestSupport {

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

  private static final ObjectMapper objectMapper = createObjectMapper();

  public void setup() throws Exception {

    if (initialized) {
      return;
    }

    initialized = true;

    // add a new mock endpoint to the beginning of errorHandlerRoute to capture the errors
    AdviceWith.adviceWith(camelContext, "errorHandlerRoute",
        a -> a.weaveAddFirst().to("mock:error"));

    var modelContext = camelContext.getCamelContextExtension().getContextPlugin(ModelCamelContext.class);

    // JPA query route for audit log verification
    var routeJpaAuditLog = new RouteDefinition();
    routeJpaAuditLog.from("direct:queryDatabaseJpa")
        .toD("jpa:com.mycompany.catalog.mcp.model.infra.jpa.postgresql.AuditLogEntity?query=${body}");
    modelContext.addRouteDefinition(routeJpaAuditLog);

    // JDBC route for database reset
    var routeJdbc = new RouteDefinition();
    routeJdbc.from("direct:queryDatabaseJdbc")
        .to("jdbc:default?useHeadersAsParameters=true");
    modelContext.addRouteDefinition(routeJdbc);

  }

  protected void resetAllMockedEndpoints() {
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
    resetPersistenceLayers("reset-postgresql.sql", "default", true);
  }

  private void resetPersistenceLayers(String fileName, String dataSource, boolean isMultiLinesSupported) throws Exception {
    String cleanedSql = readResource("/backend/sql/%s".formatted(fileName))
        .replaceAll("/\\*.*?\\*/", "")
        .replaceAll("--.*", "")
        .replaceAll("#.*", "");

    if (isMultiLinesSupported) {
      fluentProducerTemplate.to("direct:queryDatabaseJdbc")
          .withBody(cleanedSql).withHeader("dataSourceName", dataSource).request();
    } else {
      for (String statement : cleanedSql.split(";")) {
        if (!statement.trim().isEmpty()) {
          fluentProducerTemplate.to("direct:queryDatabaseJdbc")
              .withBody(statement.trim()).withHeader("dataSourceName", dataSource).request();
        }
      }
    }
  }

  protected void clearAuditLogTable() throws Exception {
    fluentProducerTemplate.to("direct:queryDatabaseJpa")
        .withBody("DELETE FROM AuditLogEntity a")
        .request();
  }

  @SuppressWarnings("unchecked")
  protected List queryAuditLogs() throws Exception {
    return (List) fluentProducerTemplate.to("direct:queryDatabaseJpa")
        .withBody("select a from AuditLogEntity a")
        .request();
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
    javaTimeModule.addSerializer(java.time.LocalDate.class,
        new LocalDateSerializer(DateTimeFormatter.ISO_DATE));
    javaTimeModule.addSerializer(java.time.OffsetDateTime.class,
        OffsetDateTimeSerializer.INSTANCE);
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
