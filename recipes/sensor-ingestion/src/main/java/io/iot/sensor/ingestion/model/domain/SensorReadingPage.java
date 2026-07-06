package io.iot.sensor.ingestion.model.domain;

import java.util.List;
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
public class SensorReadingPage {

  private List<SensorReading> readings;

  private long totalItems;

  private int page;

  private int pageSize;

  private String deviceId;

  private String from;

  private String to;
}
