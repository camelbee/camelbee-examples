package io.fintech.loan.application.service.bbtest;

import io.fintech.loan.application.service.utils.DataSeeder;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Base class for black-box tests.
 * Tests the fully dockerized application from the outside using RestAssured.
 * Framework-agnostic: works with both SpringBoot and Quarkus (JVM and native).
 *
 * <p><b>Two modes of operation:</b></p>
 * <ul>
 * <li><b>Automatic (default):</b> TestContainers starts the app + backends via Docker Compose</li>
 * <li><b>Manual:</b> Set {@code -Dbbtest.manual=true} to skip TestContainers and run against
 * a manually started app (e.g., in IntelliJ debug mode). Optionally set {@code -Dbbtest.host}
 * and {@code -Dbbtest.port} to configure the target.</li>
 * </ul>
 *
 * <pre>
 * # Automatic mode (default): build + run black-box tests
 * ./mvnw package -DskipTests
 * ./mvnw verify -Pblack-box-test
 *
 * # Manual mode: run tests against a locally running app
 * # 1. Start backends: docker compose -f src/integration-test/resources/compose-backends.yml up -d
 * # 2. Start your app in IntelliJ (debug mode)
 * # 3. Run tests with:
 * ./mvnw verify -Pblack-box-test -Dbbtest.manual=true
 * # Or with custom host/port:
 * ./mvnw verify -Pblack-box-test -Dbbtest.manual=true -Dbbtest.host=localhost -Dbbtest.port=8080
 * </pre>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class BlackBoxTest {

  private static final boolean MANUAL_MODE = Boolean.getBoolean("bbtest.manual");

  // Transaction IDs that trigger specific WireMock error stubs
  protected static final String TXID_FOR_SUCCESS = "779d2950-7f02-440a-a4a3-b36b82ec6c29";
  protected static final String TXID_FOR_REST_400_ERROR = "9d47cfe5-4bf3-41a3-abcf-9249e6b3fae4";
  protected static final String TXID_FOR_REST_404_ERROR = "6911185b-f4f0-4435-b3e2-0720fc148b63";
  protected static final String TXID_FOR_REST_500_ERROR = "6037ac1b-edae-4f82-8a8f-e7724041ae08";
  protected static final String TXID_FOR_SOAP_CLIENT_ERROR = "b7945002-b869-499d-a83a-b98943492fba";
  protected static final String TXID_FOR_SOAP_SERVER_ERROR = "0ac73dc6-12bd-44b5-9d62-1f5f1946e97a";
  protected static final String TXID_FOR_GRPC_INVALIDARGUMENT_ERROR = "0491b5b2-fe13-456d-9b24-c3f64559910a";
  protected static final String TXID_FOR_GRPC_NOTFOUND_ERROR = "55f47844-7038-4d22-8bd0-d270c1c0241f";
  protected static final String TXID_FOR_GRPC_INTERNAL_ERROR = "e9551486-1abf-416c-8c58-f3b83ec688f2";

  // Only trigger TestContainers if not in manual mode
  static {
    if (!MANUAL_MODE) {
      var ignored = TestContainerConfiguration.ENV;
    }
  }

  @BeforeAll
  void setupRestAssured() {
    if (MANUAL_MODE) {
      RestAssured.baseURI = "http://" + System.getProperty("bbtest.host", "localhost");
      RestAssured.port = Integer.parseInt(System.getProperty("bbtest.port", "8080"));
    } else {
      RestAssured.baseURI = "http://" + TestContainerConfiguration.getAppHost();
      RestAssured.port = TestContainerConfiguration.getAppPort();
    }
    RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());

    // Reset and seed test data before each test class (mirrors resetBeforeAll() in integration tests)
    try (DataSeeder seeder = new DataSeeder()) {
      seeder.resetData();
    }
  }

  // --- Resource reading helpers ---

  protected String readResource(String path) {
    try (InputStream is = getClass().getResourceAsStream(path)) {
      if (is == null) {
        throw new RuntimeException("Resource not found: " + path);
      }
      return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException("Failed to read resource: " + path, e);
    }
  }

  protected byte[] readResourceBinary(String path) {
    try (InputStream is = getClass().getResourceAsStream(path)) {
      if (is == null) {
        throw new RuntimeException("Resource not found: " + path);
      }
      return is.readAllBytes();
    } catch (IOException e) {
      throw new RuntimeException("Failed to read resource: " + path, e);
    }
  }
}