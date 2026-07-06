package io.iot.sensor.ingestion.bbtest.rest;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import io.iot.sensor.ingestion.bbtest.ListReadingsBlackBoxTest;
import io.restassured.response.Response;
import java.util.stream.Stream;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RestInterfaceListReadingsBlackBoxTest extends ListReadingsBlackBoxTest {

  static final String SENSOR_BASE_URL = "/camelbee-service/sensor-readings";

  private static Stream<Arguments> listSuccessParameters() {
    return Stream.of(
        Arguments.of("device-001", "0", "50", 200)
    );
  }

  @ParameterizedTest
  @Order(1)
  @MethodSource("listSuccessParameters")
  void given_ValidDeviceId_When_ListRouteCalled_Then_ResultIsSuccess(
      String deviceId, String page, String pageSize, int httpStatus) throws Exception {
    setupListSuccessScenario();
    Response response = callTestRoute(deviceId, page, pageSize);
    assertThat(response.statusCode()).isEqualTo(httpStatus);
    validateListSuccessScenario();
  }

  private Response callTestRoute(String deviceId, String page, String pageSize) {
    return given()
        .queryParam("deviceId", deviceId)
        .queryParam("page", page)
        .queryParam("pageSize", pageSize)
        .when()
        .get(SENSOR_BASE_URL);
  }
}
