package io.iot.sensor.ingestion.bbtest.rest;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import io.iot.sensor.ingestion.bbtest.GetReadingBlackBoxTest;
import io.restassured.response.Response;
import java.util.stream.Stream;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RestInterfaceGetReadingBlackBoxTest extends GetReadingBlackBoxTest {

  static final String SENSOR_BASE_URL = "/camelbee-service/sensor-readings";

  private static Stream<Arguments> getSuccessParameters() {
    return Stream.of(
        Arguments.of("test-reading-001", 200)
    );
  }

  @ParameterizedTest
  @Order(1)
  @MethodSource("getSuccessParameters")
  void given_ValidReadingId_When_GetRouteCalled_Then_ResultIsSuccess(
      String readingId, int httpStatus) throws Exception {
    setupGetSuccessScenario();
    Response response = callTestRoute(readingId);
    assertThat(response.statusCode()).isEqualTo(httpStatus);
    validateGetSuccessScenario();
  }

  private Response callTestRoute(String readingId) {
    return given()
        .when()
        .get(SENSOR_BASE_URL + "/" + readingId);
  }
}
