package com.mycompany.product.catalog.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Seeds test data into backend databases for black-box tests.
 * Uses direct JDBC/driver connections (no Camel context required).
 * Mirrors the resetBeforeAll() logic from IntegrationTest.
 */
public class DataSeeder implements AutoCloseable {

  private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

  /**
   * Resets and seeds all backend databases with test data.
   */
  public void resetData() {
    log.info("Resetting and seeding test data...");

    resetJpaData();

    log.info("Test data seeding completed.");
  }

  private Connection sqlSeederConnection;

  private Connection getOrCreateConnection(String jdbcUrl, String user, String password) throws Exception {
    if (sqlSeederConnection != null && !sqlSeederConnection.isClosed()) {
      return sqlSeederConnection;
    }
    sqlSeederConnection = DriverManager.getConnection(jdbcUrl, user, password);
    return sqlSeederConnection;
  }

  private void executeSqlScript(String jdbcUrl, String user, String password, String scriptPath, boolean multiLineSupported) {
    String sql = readResource(scriptPath)
        .replaceAll("/\\*.*?\\*/", "")
        .replaceAll("--.*", "")
        .replaceAll("#.*", "");

    try {
      Connection conn = getOrCreateConnection(jdbcUrl, user, password);
      try (Statement stmt = conn.createStatement()) {
        if (multiLineSupported) {
          stmt.execute(sql);
        } else {
          for (String statement : sql.split(";")) {
            if (!statement.trim().isEmpty()) {
              stmt.execute(statement.trim());
            }
          }
        }
      }
      log.info("Executed SQL script: {}", scriptPath);
    } catch (Exception e) {
      log.error("Failed to execute SQL script: {}", scriptPath, e);
      throw new RuntimeException("Failed to execute SQL script: " + scriptPath, e);
    }
  }

  private void resetJpaData() {
    log.info("Resetting JPA backend data (POSTGRESQL)...");
    executeSqlScript("jdbc:postgresql://localhost:5432/CAMELBEE_DATABASE", "camelbee_user", "secret",
        "/backend/sql/reset-postgresql.sql", true);
  }

  private String readResource(String path) {
    try (InputStream is = getClass().getResourceAsStream(path)) {
      if (is == null) {
        throw new RuntimeException("Resource not found: " + path);
      }
      return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException("Failed to read resource: " + path, e);
    }
  }

  @Override
  public void close() {
    try {
      if (sqlSeederConnection != null && !sqlSeederConnection.isClosed())
        sqlSeederConnection.close();
    } catch (Exception e) {
      log.error("Failed to close SQL seeder connection: {}", e.getMessage());
    }
  }
}
