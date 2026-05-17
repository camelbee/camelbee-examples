package io.fintech.loan.application.service.bbtest;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Black-box test stack manager. Brings up the full dockerized application
 * (app + Postgres + Kafka + Apicurio + Redis + WireMock) via the
 * compose-blackbox.yml file, then waits for the app's health endpoint
 * to respond before tests run.
 *
 * <p>The static block runs once per JVM, on first class reference.
 */
public final class TestContainerConfiguration {

  public static final TestContainerConfiguration ENV = new TestContainerConfiguration();

  private static final File COMPOSE_FILE = new File(
      "src/black-box-test/resources/compose-blackbox.yml").getAbsoluteFile();

  private static final boolean MANUAL_MODE = Boolean.getBoolean("bbtest.manual");

  static {
    if (!MANUAL_MODE) {
      try {
        composeUp();
        waitForApp();
      } catch (Exception e) {
        throw new RuntimeException("Failed to start black-box stack: " + e.getMessage(), e);
      }
    }
  }

  private TestContainerConfiguration() {
  }

  private static void composeUp() throws IOException, InterruptedException {
    System.out.println("[BlackBoxStack] Starting via " + COMPOSE_FILE);
    Process up = new ProcessBuilder("docker", "compose", "-f", COMPOSE_FILE.getPath(), "up", "-d")
        .redirectErrorStream(true)
        .start();
    String out = new String(up.getInputStream().readAllBytes());
    if (!up.waitFor(10, TimeUnit.MINUTES)) {
      up.destroyForcibly();
      throw new IllegalStateException("docker compose up timed out\n" + out);
    }
    if (up.exitValue() != 0) {
      throw new IllegalStateException("docker compose up failed (exit " + up.exitValue() + ")\n" + out);
    }
    System.out.println(out);
  }

  private static void waitForApp() throws InterruptedException {
    System.out.println("[BlackBoxStack] Waiting for app health on http://localhost:8080/health ...");
    long deadline = System.currentTimeMillis() + 180_000L;
    while (System.currentTimeMillis() < deadline) {
      try {
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) new java.net.URI("http://localhost:8080/health").toURL().openConnection();
        conn.setConnectTimeout(2000);
        conn.setReadTimeout(2000);
        int code = conn.getResponseCode();
        if (code >= 200 && code < 400) {
          System.out.println("[BlackBoxStack] App is healthy (HTTP " + code + ")");
          return;
        }
      } catch (Exception ignored) {
        // not ready yet
      }
      Thread.sleep(2000);
    }
    throw new IllegalStateException("Timed out waiting for app health endpoint");
  }
}
