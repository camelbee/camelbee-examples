package io.iot.sensor.ingestion.model.domain;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class SensorReading {

  private String readingId;

  private String deviceId;

  private SensorType sensorType;

  private Double value;

  private String unit;

  private ReadingQuality quality;

  private String location;

  private Instant recordedAt;

  private Instant receivedAt;
}
