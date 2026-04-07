package com.mycompany.product.catalog.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Verifies backend database state for black-box tests.
 * Uses direct JDBC connections (no Camel context required).
 */
public class DataVerifier implements AutoCloseable {

  private static final Logger log = LoggerFactory.getLogger(DataVerifier.class);

  private Connection jpaConnection;

  private static final String JPA_JDBC_URL = "jdbc:postgresql://localhost:5432/CAMELBEE_DATABASE";
  private static final String JPA_USER = "camelbee_user";
  private static final String JPA_PASSWORD = "secret";
  private static final String JPA_SCHEMA = "camelbee_user";

  public int countAuditLogs() {
    return countTable(JPA_JDBC_URL, JPA_USER, JPA_PASSWORD, JPA_SCHEMA, "CAMELBEE_AUDIT_LOG_TABLE");
  }

  public void clearAuditLogTable() {
    executeUpdate(JPA_JDBC_URL, JPA_USER, JPA_PASSWORD, JPA_SCHEMA, "DELETE FROM CAMELBEE_AUDIT_LOG_TABLE");
  }

  private Connection getOrCreateConnection(Connection cached, String jdbcUrl, String user, String password) throws Exception {
    if (cached != null && !cached.isClosed()) {
      return cached;
    }
    return DriverManager.getConnection(jdbcUrl, user, password);
  }

  private int countTable(String jdbcUrl, String user, String password, String schema, String tableName) {
    String sql = "SELECT COUNT(*) FROM " + tableName;
    try {
      jpaConnection = getOrCreateConnection(jpaConnection, jdbcUrl, user, password);
      try (Statement stmt = jpaConnection.createStatement()) {
        if (schema != null) {
          stmt.execute("SET search_path TO " + schema);
        }
        ResultSet rs = stmt.executeQuery(sql);
        rs.next();
        return rs.getInt(1);
      }
    } catch (Exception e) {
      log.error("Failed to count table {}: {}", tableName, e.getMessage());
      throw new RuntimeException(e);
    }
  }

  private void executeUpdate(String jdbcUrl, String user, String password, String schema, String sql) {
    try {
      jpaConnection = getOrCreateConnection(jpaConnection, jdbcUrl, user, password);
      try (Statement stmt = jpaConnection.createStatement()) {
        if (schema != null) {
          stmt.execute("SET search_path TO " + schema);
        }
        stmt.executeUpdate(sql);
      }
    } catch (Exception e) {
      log.error("Failed to execute SQL {}: {}", sql, e.getMessage());
      throw new RuntimeException(e);
    }
  }

  @Override
  public void close() {
    try {
      if (jpaConnection != null && !jpaConnection.isClosed())
        jpaConnection.close();
    } catch (Exception e) {
      log.error("Failed to close JPA connection: {}", e.getMessage());
    }
  }
}
