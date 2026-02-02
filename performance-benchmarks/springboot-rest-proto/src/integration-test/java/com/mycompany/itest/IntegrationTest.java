package com.mycompany.itest;

import jakarta.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.FluentProducerTemplate;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration Tester with TestContainers.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"itest"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
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

  private static boolean initialized = false;

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

}
