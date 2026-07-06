package io.iot.sensor.ingestion.utils.testdata;

import io.iot.sensor.ingestion.model.domain.Error;
import io.iot.sensor.ingestion.model.domain.SensorReading;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RequestResponseScenario {

  private String name;
  private SensorReading reading;
  private List<SensorReading> readings;
  private Error error;
  private String page;
  private String pageSize;
  private String deviceId;
  private String from;
  private String to;
}
