package io.fintech.loan.application.service.bbtest;

import io.restassured.RestAssured;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Base for black-box tests. The full dockerized stack (app + backends) is
 * booted once per JVM by {@link TestContainerConfiguration}. Tests speak to
 * the published REST port (8080) and the published JDBC port (5432) on
 * localhost.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class BlackBoxTest {

  protected static final String APP_HOST = System.getProperty("bbtest.host", "localhost");
  protected static final int APP_PORT = Integer.parseInt(System.getProperty("bbtest.port", "8080"));

  protected static final String JDBC_URL = System.getProperty("bbtest.jdbc.url", "jdbc:postgresql://localhost:5432/CAMELBEE_DATABASE");
  protected static final String JDBC_USER = System.getProperty("bbtest.jdbc.user", "camelbee_user");
  protected static final String JDBC_PASS = System.getProperty("bbtest.jdbc.pass", "secret");

  static {
    // Force the stack to boot before any test method runs.
    TestContainerConfiguration env = TestContainerConfiguration.ENV;
    if (env == null) {
      throw new IllegalStateException("stack not initialised");
    }
    RestAssured.baseURI = "http://" + APP_HOST;
    RestAssured.port = APP_PORT;
  }

  protected Connection openJdbc() throws SQLException {
    return DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS);
  }

  protected void truncateLoanApplications() throws SQLException {
    try (Connection c = openJdbc(); Statement s = c.createStatement()) {
      s.execute("TRUNCATE TABLE camelbee_user.loan_applications");
    }
  }

  protected int countByApplicantId(String applicantId) throws SQLException {
    try (Connection c = openJdbc(); PreparedStatement ps = c.prepareStatement(
        "SELECT COUNT(*) FROM camelbee_user.loan_applications WHERE applicant_id = ?")) {
      ps.setString(1, applicantId);
      var rs = ps.executeQuery();
      rs.next();
      return rs.getInt(1);
    }
  }

  protected String statusByApplicationId(String applicationId) throws SQLException {
    try (Connection c = openJdbc(); PreparedStatement ps = c.prepareStatement(
        "SELECT status FROM camelbee_user.loan_applications WHERE application_id = ?")) {
      ps.setString(1, applicationId);
      var rs = ps.executeQuery();
      return rs.next() ? rs.getString(1) : null;
    }
  }
}
