
package io.fintech.loan.application.service.itest;

import java.io.File;
import java.time.Duration;
import org.springframework.boot.test.context.TestConfiguration;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Configuration for initializing backend docker images for integration test.
 */
@TestConfiguration
@Testcontainers
public class TestContainerConfiguration {

  @Container
  public static final DockerComposeContainer ENV = new DockerComposeContainer(
      new File("src/integration-test/resources/compose-backends.yml"))
      .withOptions("--compatibility");

  static {

    ENV.withExposedService("flyway-postgresql", 0, Wait.forLogMessage(".*" + "Database is ready" + ".*", 1));

    ENV.withExposedService("broker", 9092, Wait.forListeningPort());

    ENV.withExposedService("cache", 6379,
        Wait.forLogMessage(".*Ready to accept connections.*", 1).withStartupTimeout(Duration.ofMinutes(2)));

    ENV.withExposedService("wiremock", 8080, Wait.forListeningPort());

    ENV.start();

  }

}
