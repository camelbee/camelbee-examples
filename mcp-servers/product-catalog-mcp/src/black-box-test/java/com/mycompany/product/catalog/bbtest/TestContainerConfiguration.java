package com.mycompany.product.catalog.bbtest;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

/**
 * TestContainers configuration for black-box tests.
 * Starts the application as a Docker container alongside backend services.
 *
 * <p>Uses {@code .withLocalCompose(true)} which means Docker Compose manages port mappings
 * directly (as defined in the compose file), not TestContainers. Ports are fixed, not random.</p>
 *
 * <p><b>Prerequisites:</b> The application must be packaged before running black-box tests,
 * as the Docker image is built from the artifacts in {@code target/}.</p>
 *
 * <pre>
 * # Build the application first
 * ./mvnw package -DskipTests
 *
 * # Then run black-box tests
 * ./mvnw verify -Pblack-box-test
 *
 * # For native mode
 * ./mvnw package -Pnative -DskipTests
 * ./mvnw verify -Pblack-box-test -Dbbtest.native=true
 * </pre>
 */
public class TestContainerConfiguration {

  private static final String COMPOSE_FILE = System.getProperty("bbtest.native", "false").equals("true")
      ? "src/black-box-test/resources/compose-blackbox-native.yml"
      : "src/black-box-test/resources/compose-blackbox.yml";

  /** Application port as defined in the compose file. */
  private static final int APP_PORT = 8080;

  /** Maximum time to wait for the application health endpoint. */
  private static final Duration APP_STARTUP_TIMEOUT = Duration.ofMinutes(10);

  public static final DockerComposeContainer ENV = new DockerComposeContainer(
      new File(COMPOSE_FILE))
      .withLocalCompose(true)
      .withOptions("--compatibility");

  static {

    ENV.withExposedService("flyway-postgresql", 0, Wait.forLogMessage(".*" + "Database is ready" + ".*", 1));

    ENV.withExposedService("wiremock", 8080, Wait.forListeningPort());

    ENV.start();

    // Wait for the application health endpoint (uses fixed port from compose file).
    // Quarkus: use /health/live (liveness only) — Camel registers route health checks as
    // readiness probes, and those call blocking operations (e.g. Kafka lock, Cassandra CQL)
    // on the vert.x event loop thread, which hangs the HTTP response entirely.
    // Liveness is a lightweight "HTTP server is up" check with no Camel involvement.
    // SpringBoot: /health works fine because health checks run on a separate thread pool.
    waitForHealthEndpoint("http://localhost:" + APP_PORT + "/health/live", APP_STARTUP_TIMEOUT);

  }

  /**
   * Waits for the application health endpoint to return HTTP 200.
   */
  private static void waitForHealthEndpoint(String url, Duration timeout) {
    long deadline = System.currentTimeMillis() + timeout.toMillis();
    while (System.currentTimeMillis() < deadline) {
      try {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(20000);
        connection.setReadTimeout(20000);
        int responseCode = connection.getResponseCode();
        connection.disconnect();
        if (responseCode == 200) {
          return;
        }
      } catch (Exception e) {
        // Not ready yet
      }
      try {
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException("Interrupted while waiting for health endpoint", e);
      }
    }
    throw new RuntimeException("Timed out waiting for health endpoint: " + url);
  }

  /**
   * Returns the application port (fixed, as defined in compose file).
   */
  public static int getAppPort() {
    return APP_PORT;
  }

  /**
   * Returns the application host.
   */
  public static String getAppHost() {
    return "localhost";
  }

}