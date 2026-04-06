package com.mycompany.catalog.mcp.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Verifies backend database state for black-box tests.
 * Uses direct JDBC/driver connections (no Camel context required).
 * Mirrors the query/count methods from IntegrationTest.
 *
 * <p>Connections are lazily initialized and reused across calls.
 * Call {@link #close()} (e.g. in {@code @AfterAll}) to release resources.</p>
 */
public class DataVerifier implements AutoCloseable {

  private static final Logger log = LoggerFactory.getLogger(DataVerifier.class);


  // --- JPA Backend Verification ---

  private Connection jpaConnection;

  private static final String JPA_JDBC_URL = "jdbc:postgresql://localhost:5432/CAMELBEE_DATABASE";
  private static final String JPA_USER = "camelbee_user";
  private static final String JPA_PASSWORD = "secret";
  private static final String JPA_SCHEMA = "camelbee_user";

  public int countJpaPurchases() {
    return countTable(JPA_JDBC_URL, JPA_USER, JPA_PASSWORD, JPA_SCHEMA, "CAMELBEE_PURCHASES_TABLE_JPA", false);
  }

  public int countJpaPurchaseItems() {
    return countTable(JPA_JDBC_URL, JPA_USER, JPA_PASSWORD, JPA_SCHEMA, "CAMELBEE_PURCHASEITEMS_TABLE_JPA", false);
  }

  public int countJpaPurchases(String whereClause) {
    return countTableWhere(JPA_JDBC_URL, JPA_USER, JPA_PASSWORD, JPA_SCHEMA, "CAMELBEE_PURCHASES_TABLE_JPA", whereClause, false);
  }

  public void clearJpaTables() {
    executeUpdate(JPA_JDBC_URL, JPA_USER, JPA_PASSWORD, JPA_SCHEMA, "DELETE FROM CAMELBEE_PURCHASEITEMS_TABLE_JPA", false);
    executeUpdate(JPA_JDBC_URL, JPA_USER, JPA_PASSWORD, JPA_SCHEMA, "DELETE FROM CAMELBEE_PURCHASES_TABLE_JPA", false);
  }

  // --- Common JDBC helpers ---

  private Connection getOrCreateConnection(Connection cached, String jdbcUrl, String user, String password) throws Exception {
    if (cached != null && !cached.isClosed()) {
      return cached;
    }
    return DriverManager.getConnection(jdbcUrl, user, password);
  }

  private int countTable(String jdbcUrl, String user, String password, String schema, String tableName, boolean isSql) {
    String sql = "SELECT COUNT(*) FROM " + tableName;
    try {
      Connection conn = isSql
          ? (sqlConnection = getOrCreateConnection(sqlConnection, jdbcUrl, user, password))
          : (jpaConnection = getOrCreateConnection(jpaConnection, jdbcUrl, user, password));
      try (Statement stmt = conn.createStatement()) {
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

  private int countTableWhere(String jdbcUrl, String user, String password, String schema, String tableName, String whereClause, boolean isSql) {
    String sql = "SELECT COUNT(*) FROM " + tableName + " WHERE " + whereClause;
    try {
      Connection conn = isSql
          ? (sqlConnection = getOrCreateConnection(sqlConnection, jdbcUrl, user, password))
          : (jpaConnection = getOrCreateConnection(jpaConnection, jdbcUrl, user, password));
      try (Statement stmt = conn.createStatement()) {
        if (schema != null) {
          stmt.execute("SET search_path TO " + schema);
        }
        ResultSet rs = stmt.executeQuery(sql);
        rs.next();
        return rs.getInt(1);
      }
    } catch (Exception e) {
      log.error("Failed to count table {} with where {}: {}", tableName, whereClause, e.getMessage());
      throw new RuntimeException(e);
    }
  }

  private void executeUpdate(String jdbcUrl, String user, String password, String schema, String sql, boolean isSql) {
    try {
      Connection conn = isSql
          ? (sqlConnection = getOrCreateConnection(sqlConnection, jdbcUrl, user, password))
          : (jpaConnection = getOrCreateConnection(jpaConnection, jdbcUrl, user, password));
      try (Statement stmt = conn.createStatement()) {
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
    try { if (jpaConnection != null && !jpaConnection.isClosed()) jpaConnection.close(); }
    catch (Exception e) { log.error("Failed to close JPA connection: {}", e.getMessage()); }
  }

}
