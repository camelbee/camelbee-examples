package io.iot.sensor.ingestion.bbtest;

import io.iot.sensor.ingestion.utils.DataSeeder;
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

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class BlackBoxTest {

  private static final boolean MANUAL_MODE = Boolean.getBoolean("bbtest.manual");

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

    try (DataSeeder seeder = new DataSeeder()) {
      seeder.resetData();
    }
  }

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
