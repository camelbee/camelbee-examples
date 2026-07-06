package io.iot.sensor.ingestion.utils.testdata;

import io.iot.sensor.ingestion.model.domain.ReadingQuality;
import io.iot.sensor.ingestion.model.domain.SensorReading;
import io.iot.sensor.ingestion.model.domain.SensorType;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SensorReadingTestDataProducer {

  public static final class RequestScenarios {

    public static final String INGEST_READING_SUCCESS = "ingest-reading-success-request";
    public static final String INGEST_READING_INVALID = "ingest-reading-invalid-request";
    public static final String LIST_READINGS_SUCCESS = "list-readings-success-request";
    public static final String LIST_READINGS_INVALID_DEVICE_ID = "list-readings-invalid-deviceId-request";
    public static final String GET_READING_SUCCESS = "get-reading-success-request";
    public static final String GET_READING_NOT_FOUND = "get-reading-notfound-request";
    public static final String GET_READING_NULL_ID = "get-reading-null-id-request";
  }

  public static List<RequestResponseScenario> generateIngestReadingRequests() {
    List<RequestResponseScenario> scenarios = new ArrayList<>();
    scenarios.add(RequestResponseScenario.builder()
        .name(RequestScenarios.INGEST_READING_SUCCESS)
        .reading(createReading("device-001", SensorType.TEMPERATURE, 23.5, "C", ReadingQuality.GOOD, null))
        .build());
    scenarios.add(RequestResponseScenario.builder()
        .name(RequestScenarios.INGEST_READING_INVALID)
        .reading(null)
        .build());
    return scenarios;
  }

  public static List<RequestResponseScenario> generateListReadingsRequests() {
    List<RequestResponseScenario> scenarios = new ArrayList<>();
    scenarios.add(RequestResponseScenario.builder()
        .name(RequestScenarios.LIST_READINGS_SUCCESS)
        .deviceId("device-001")
        .page("0")
        .pageSize("50")
        .build());
    scenarios.add(RequestResponseScenario.builder()
        .name(RequestScenarios.LIST_READINGS_INVALID_DEVICE_ID)
        .deviceId(null)
        .page("0")
        .pageSize("50")
        .build());
    return scenarios;
  }

  public static List<RequestResponseScenario> generateGetReadingRequests() {
    List<RequestResponseScenario> scenarios = new ArrayList<>();
    scenarios.add(RequestResponseScenario.builder()
        .name(RequestScenarios.GET_READING_SUCCESS)
        .reading(createReading("device-001", SensorType.TEMPERATURE, 23.5, "C", ReadingQuality.GOOD, "room-1"))
        .build());
    scenarios.add(RequestResponseScenario.builder()
        .name(RequestScenarios.GET_READING_NOT_FOUND)
        .reading(createReading("device-999", SensorType.TEMPERATURE, 23.5, "C", ReadingQuality.GOOD, null))
        .build());
    scenarios.add(RequestResponseScenario.builder()
        .name(RequestScenarios.GET_READING_NULL_ID)
        .reading(null)
        .build());
    return scenarios;
  }

  private static SensorReading createReading(String deviceId, SensorType type, double value, String unit, ReadingQuality quality, String location) {
    SensorReading reading = new SensorReading();
    reading.setReadingId(UUID.randomUUID().toString());
    reading.setDeviceId(deviceId);
    reading.setSensorType(type);
    reading.setValue(value);
    reading.setUnit(unit);
    reading.setQuality(quality);
    reading.setLocation(location);
    reading.setRecordedAt(Instant.now());
    reading.setReceivedAt(Instant.now());
    return reading;
  }
}
