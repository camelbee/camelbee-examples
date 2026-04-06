
package com.mycompany.catalog.mcp.itest;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;

public class TestContainerConfiguration implements QuarkusTestResourceLifecycleManager {

  @Container
  static final DockerComposeContainer ENV = new DockerComposeContainer(
      new File("src/integration-test/resources/compose-backends.yml"))
      .withOptions("--compatibility");

  static {

    ENV.withExposedService("flyway-postgresql", 0, Wait.forLogMessage(".*" + "Database is ready" + ".*", 1));

    ENV.withExposedService("wiremock", 8080, Wait.forListeningPort());

  }

  @Override
  public Map<String, String> start() {
    var configOverrides = new HashMap<String, String>();

    ENV.start();

    return configOverrides;
  }

  @Override
  public void stop() {
    // Managed by the @Container and @Testcontainer tag
  }

}
