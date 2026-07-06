package io.iot.sensor.ingestion.bbtest;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

public class TestContainerConfiguration {

  private static final String COMPOSE_FILE = System.getProperty("bbtest.native", "false").equals("true")
      ? "src/black-box-test/resources/compose-blackbox-native.yml"
      : "src/black-box-test/resources/compose-blackbox.yml";

  private static final int APP_PORT = 8080;
  private static final Duration APP_STARTUP_TIMEOUT = Duration.ofMinutes(10);

  public static final DockerComposeContainer ENV = new DockerComposeContainer(
      new File(COMPOSE_FILE))
      .withOptions("--compatibility");

  static {
    ENV.withExposedService("broker", 9092, Wait.forListeningPort());
    ENV.withExposedService("mongodb", 27017, Wait.forLogMessage(".*" + "mongod startup complete" + ".*", 1));
    ENV.withExposedService("mosquitto", 1883, Wait.forLogMessage(".*" + "mosquitto version" + ".*", 1));
    ENV.withExposedService("mosquitto", 1883, Wait.forLogMessage(".*" + "running" + ".*", 1));
    ENV.start();
    waitForHealthEndpoint("http://localhost:" + APP_PORT + "/health/live", APP_STARTUP_TIMEOUT);
  }

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

  public static int getAppPort() {
    return APP_PORT;
  }

  public static String getAppHost() {
    return "localhost";
  }
}
