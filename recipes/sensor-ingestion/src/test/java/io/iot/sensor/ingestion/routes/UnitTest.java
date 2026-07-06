package io.iot.sensor.ingestion.routes;

import io.iot.sensor.ingestion.model.domain.SensorReading;
import io.iot.sensor.ingestion.utils.testdata.RequestResponseScenario;
import io.quarkus.test.junit.QuarkusTestProfile;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.apache.camel.CamelContext;
import org.apache.camel.FluentProducerTemplate;
import org.apache.camel.quarkus.test.CamelQuarkusTestSupport;

public abstract class UnitTest extends CamelQuarkusTestSupport implements QuarkusTestProfile {

  @Inject
  protected FluentProducerTemplate fluentProducerTemplate;

  @Inject
  protected CamelContext camelContext;

  protected SensorReading getReadingByScenarioName(List<RequestResponseScenario> scenarios, String scenarioName) {
    Optional<RequestResponseScenario> scenario = scenarios.stream()
        .filter(s -> s.getName().equals(scenarioName))
        .findFirst();
    return scenario.orElseThrow(() -> new NoSuchElementException("No scenario found with name: " + scenarioName))
        .getReading();
  }

  protected void setHeaderFromScenario(Map<String, Object> headers, List<RequestResponseScenario> scenarios,
      String scenarioName, String headerName) {
    scenarios.stream()
        .filter(s -> s.getName().equals(scenarioName))
        .findFirst()
        .ifPresent(scenario -> {
          switch (headerName) {
            case "deviceId" -> {
              if (scenario.getDeviceId() != null) {
                headers.put(headerName, scenario.getDeviceId());
              }
            }
            case "page" -> {
              if (scenario.getPage() != null) {
                headers.put(headerName, scenario.getPage());
              }
            }
            case "pageSize" -> {
              if (scenario.getPageSize() != null) {
                headers.put(headerName, scenario.getPageSize());
              }
            }
            case "from" -> {
              if (scenario.getFrom() != null) {
                headers.put(headerName, scenario.getFrom());
              }
            }
            case "to" -> {
              if (scenario.getTo() != null) {
                headers.put(headerName, scenario.getTo());
              }
            }
            default -> {
            }
          }
        });
  }

  @Override
  public String getConfigProfile() {
    return "test";
  }
}
