package io.iot.sensor.ingestion.routes.producer.mongodb;

import static org.junit.jupiter.api.Assertions.*;

import io.iot.sensor.ingestion.constants.Constants;
import io.iot.sensor.ingestion.model.domain.ReadingQuality;
import io.iot.sensor.ingestion.model.domain.SensorReading;
import io.iot.sensor.ingestion.model.domain.SensorType;
import io.iot.sensor.ingestion.routes.UnitTest;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.apache.camel.Exchange;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.builder.ExchangeBuilder;
import org.camelbee.config.CamelBeeRouteConfigurer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@QuarkusTest
@TestProfile(MongodbProducerRouteUnitTest.class)
@TestInstance(Lifecycle.PER_CLASS)
public class MongodbProducerRouteUnitTest extends UnitTest {

  @Inject
  CamelBeeRouteConfigurer camelBeeRouteConfigurer;

  @BeforeAll
  public void setup() throws Exception {
    MongodbProducerRoute route = new MongodbProducerRoute(camelBeeRouteConfigurer);
    camelContext.addRoutes(route);

    AdviceWith.adviceWith(camelContext, "ingestReadingMongoDbRoute", a -> a.mockEndpointsAndSkip("mongodb*"));
    AdviceWith.adviceWith(camelContext, "listReadingsMongoDbRoute", a -> a.mockEndpointsAndSkip("mongodb*"));
    AdviceWith.adviceWith(camelContext, "getReadingMongoDbRoute", a -> a.mockEndpointsAndSkip("mongodb*"));

    camelContext.start();
  }

  @Test
  @Order(1)
  void given_ReadingWithAllFields_When_Ingest_Then_Success() throws Exception {
    SensorReading reading = createReading("device-001", SensorType.TEMPERATURE, 23.5,
        "C", ReadingQuality.GOOD, "room-1", Instant.parse("2025-01-01T00:00:00Z"));
    Exchange result = sendBody("direct:ingestReadingMongoDb", reading);
    assertNotNull(result);
    assertNull(result.getException());
  }

  @Test
  @Order(2)
  void given_ReadingWithNullSensorTypeAndQuality_When_Ingest_Then_Success() throws Exception {
    SensorReading reading = createReading("device-002", null, 23.5, "C", null, null, null);
    Exchange result = sendBody("direct:ingestReadingMongoDb", reading);
    assertNotNull(result);
    assertNull(result.getException());
  }

  @Test
  @Order(3)
  void given_NullPageAndPageSize_When_List_Then_UseDefaults() throws Exception {
    Map<String, Object> headers = new HashMap<>();
    headers.put("deviceId", "device-001");
    Exchange result = sendToDirectWithProperty("direct:listReadingsMongoDb", headers, null);
    assertNotNull(result);
    assertNull(result.getException());
  }

  @Test
  @Order(4)
  void given_NegativePage_When_List_Then_UseDefaultPage() throws Exception {
    Map<String, Object> headers = new HashMap<>();
    headers.put("deviceId", "device-001");
    headers.put("page", -1);
    headers.put("pageSize", 50);
    Exchange result = sendToDirectWithProperty("direct:listReadingsMongoDb", headers, null);
    assertNotNull(result);
    assertNull(result.getException());
  }

  @Test
  @Order(5)
  void given_ZeroPageSize_When_List_Then_UseDefaultPageSize() throws Exception {
    Map<String, Object> headers = new HashMap<>();
    headers.put("deviceId", "device-001");
    headers.put("page", 0);
    headers.put("pageSize", 0);
    Exchange result = sendToDirectWithProperty("direct:listReadingsMongoDb", headers, null);
    assertNotNull(result);
    assertNull(result.getException());
  }

  @Test
  @Order(6)
  void given_FromAndToTimeRange_When_List_Then_Success() throws Exception {
    Map<String, Object> headers = new HashMap<>();
    headers.put("deviceId", "device-001");
    headers.put("page", 0);
    headers.put("pageSize", 50);
    headers.put("from", "2025-01-01T00:00:00Z");
    headers.put("to", "2025-12-31T23:59:59Z");
    Exchange result = sendToDirectWithProperty("direct:listReadingsMongoDb", headers, null);
    assertNotNull(result);
    assertNull(result.getException());
  }

  @Test
  @Order(7)
  void given_OnlyFromTimeRange_When_List_Then_Success() throws Exception {
    Map<String, Object> headers = new HashMap<>();
    headers.put("deviceId", "device-001");
    headers.put("page", 0);
    headers.put("pageSize", 50);
    headers.put("from", "2025-01-01T00:00:00Z");
    Exchange result = sendToDirectWithProperty("direct:listReadingsMongoDb", headers, null);
    assertNotNull(result);
    assertNull(result.getException());
  }

  @Test
  @Order(8)
  void given_ValidReadingId_When_Get_Then_Success() throws Exception {
    Map<String, Object> headers = new HashMap<>();
    headers.put("readingId", "test-reading-001");
    Exchange exchange = ExchangeBuilder.anExchange(camelContext).build();
    exchange.getIn().setHeaders(headers);
    Exchange result = fluentProducerTemplate
        .to("direct:getReadingMongoDb").withExchange(exchange).send();
    assertNotNull(result);
    assertNull(result.getException());
  }

  private SensorReading createReading(String deviceId, SensorType type, Double value,
      String unit, ReadingQuality quality, String location, Instant recordedAt) {
    SensorReading reading = new SensorReading();
    reading.setReadingId(UUID.randomUUID().toString());
    reading.setDeviceId(deviceId);
    reading.setSensorType(type);
    reading.setValue(value);
    reading.setUnit(unit);
    reading.setQuality(quality);
    reading.setLocation(location);
    reading.setRecordedAt(recordedAt);
    reading.setReceivedAt(Instant.now());
    return reading;
  }

  private Exchange sendBody(String endpoint, Object body) throws Exception {
    Exchange exchange = ExchangeBuilder.anExchange(camelContext).build();
    exchange.getIn().setBody(body);
    exchange.setProperty(Constants.ORIGINAL_BODY, body);
    return fluentProducerTemplate.to(endpoint).withExchange(exchange).send();
  }

  private Exchange sendToDirectWithProperty(String endpoint, Map<String, Object> headers, Object body) throws Exception {
    Exchange exchange = ExchangeBuilder.anExchange(camelContext).build();
    exchange.getIn().setHeaders(headers);
    exchange.getIn().setBody(body);
    exchange.setProperty(Constants.ORIGINAL_BODY, body);
    return fluentProducerTemplate.to(endpoint).withExchange(exchange).send();
  }
}
