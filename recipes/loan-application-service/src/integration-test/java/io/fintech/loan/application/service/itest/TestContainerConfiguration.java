package io.fintech.loan.application.service.itest;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.test.context.TestConfiguration;

/**
 * Boots the backend stack from compose-backends.yml via the local docker-compose CLI.
 *
 * <p>We deliberately avoid TestContainers' {@code ComposeContainer} because its
 * ambassador-container approach can't reliably attach to short-lived services
 * (the one-shot flyway-postgresql migration container exits cleanly with code 0,
 * which the ambassador interprets as a startup failure).
 *
 * <p>The compose file publishes static host ports, so application.yml's localhost
 * defaults connect without per-test remapping.
 */
@TestConfiguration
public class TestContainerConfiguration {

  private static final java.io.File COMPOSE_FILE = new java.io.File(
      "src/integration-test/resources/compose-backends.yml").getAbsoluteFile();

  static {
    try {
      composeUp();
      // No shutdown hook: leave the stack up between maven invocations so iterative
      // test runs are fast. Tear down explicitly via `docker compose -f
      // src/integration-test/resources/compose-backends.yml down -v`.
    } catch (Exception e) {
      throw new RuntimeException("Failed to start backend stack: " + e.getMessage(), e);
    }
  }

  private static void composeUp() throws IOException, InterruptedException {
    System.out.println("[TestContainerConfiguration] Starting backend stack via docker compose: " + COMPOSE_FILE);
    Process up = new ProcessBuilder("docker", "compose", "-f", COMPOSE_FILE.getPath(), "up", "-d")
        .redirectErrorStream(true)
        .start();
    String composeOut = new String(up.getInputStream().readAllBytes());
    if (!up.waitFor(5, TimeUnit.MINUTES)) {
      up.destroyForcibly();
      throw new IllegalStateException("docker compose up timed out after 5 minutes\n" + composeOut);
    }
    if (up.exitValue() != 0) {
      throw new IllegalStateException(
          "docker compose up failed with exit code " + up.exitValue() + "\n--- compose output ---\n"
              + composeOut + "\n----------------------");
    }
    System.out.println(composeOut);

    // Wait until the postgres + broker + schema-registry + cache are all listening.
    waitForPort("localhost", 5432, "postgresql");
    waitForPort("localhost", 9092, "broker");
    waitForPort("localhost", 8081, "schema-registry");
    waitForPort("localhost", 6379, "cache");
    waitForPort("localhost", 8091, "wiremock");

    // Give flyway a chance to finish.
    waitForFlyway();

    System.out.println("[TestContainerConfiguration] Backend stack ready.");
  }

  private static void waitForPort(String host, int port, String name) throws InterruptedException {
    long deadline = System.currentTimeMillis() + 120_000L;
    while (System.currentTimeMillis() < deadline) {
      try (java.net.Socket s = new java.net.Socket()) {
        s.connect(new java.net.InetSocketAddress(host, port), 2000);
        System.out.println("[TestContainerConfiguration] " + name + " is reachable on " + port);
        return;
      } catch (Exception e) {
        Thread.sleep(1000);
      }
    }
    throw new IllegalStateException("Timed out waiting for " + name + " on " + host + ":" + port);
  }

  private static void waitForFlyway() throws IOException, InterruptedException {
    long deadline = System.currentTimeMillis() + 60_000L;
    while (System.currentTimeMillis() < deadline) {
      Process ps = new ProcessBuilder("docker", "compose", "-f", COMPOSE_FILE.getPath(),
          "ps", "--all", "--format", "{{.Service}}|{{.State}}|{{.ExitCode}}")
          .redirectErrorStream(true)
          .start();
      String out = new String(ps.getInputStream().readAllBytes());
      ps.waitFor(10, TimeUnit.SECONDS);
      for (String line : out.split("\n")) {
        if (line.startsWith("flyway-postgresql|")) {
          String[] parts = line.split("\\|");
          if (parts.length >= 3 && "exited".equalsIgnoreCase(parts[1]) && "0".equals(parts[2])) {
            System.out.println("[TestContainerConfiguration] flyway-postgresql migration completed");
            return;
          }
        }
      }
      Thread.sleep(2000);
    }
    throw new IllegalStateException("Timed out waiting for flyway-postgresql to complete");
  }

  private static void composeDown() {
    try {
      System.out.println("[TestContainerConfiguration] Tearing down backend stack ...");
      new ProcessBuilder("docker", "compose", "-f", COMPOSE_FILE.getPath(), "down", "-v")
          .inheritIO()
          .start()
          .waitFor(2, TimeUnit.MINUTES);
    } catch (Exception ignored) {
      // best-effort cleanup
    }
  }
}
