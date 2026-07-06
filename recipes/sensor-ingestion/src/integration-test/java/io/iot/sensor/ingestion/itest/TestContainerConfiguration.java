package io.iot.sensor.ingestion.itest;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

public class TestContainerConfiguration implements QuarkusTestResourceLifecycleManager {

  static final DockerComposeContainer ENV = new DockerComposeContainer(
      new File("src/integration-test/resources/compose-backends.yml"))
      .withOptions("--compatibility");

  static {
    ENV.withExposedService("broker", 9092, Wait.forListeningPort());
    ENV.withExposedService("mongodb", 27017, Wait.forLogMessage(".*" + "mongod startup complete" + ".*", 1));
    ENV.withExposedService("mosquitto", 1883, Wait.forLogMessage(".*" + "mosquitto version" + ".*", 1));
    ENV.withExposedService("mosquitto", 1883, Wait.forLogMessage(".*" + "running" + ".*", 1));
  }

  @Override
  public Map<String, String> start() {
    var configOverrides = new HashMap<String, String>();
    ENV.start();
    return configOverrides;
  }

  @Override
  public void stop() {
  }
}
