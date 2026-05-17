package io.fintech.loan.application.service.itest;

import io.fintech.loan.application.service.CamelbeeServiceApplication;
import org.apache.camel.CamelContext;
import org.apache.camel.FluentProducerTemplate;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Base for integration tests. The Docker stack is booted once for the entire JVM
 * by {@link TestContainerConfiguration}; per-class state is reset by subclasses.
 */
@SpringBootTest(classes = CamelbeeServiceApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "camelbee.tracer-enabled=false",
        "camelbee.context-enabled=false",
        "spring.main.allow-bean-definition-overriding=true"
    })
@ActiveProfiles("itest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Import(TestContainerConfiguration.class)
public abstract class IntegrationTest {

  @Autowired
  protected CamelContext camelContext;

  @Autowired
  protected FluentProducerTemplate fluentProducerTemplate;

  @LocalServerPort
  protected int port;

  @DynamicPropertySource
  static void overrideBackendUrls(DynamicPropertyRegistry registry) {
    // Make the backend REST URL point at WireMock's published port so
    // calls to the credit bureau land on the WireMock stubs.
    registry.add("backend-credit-bureau-api.url",
        () -> "http://localhost:8091/credit-assessments");
  }
}
