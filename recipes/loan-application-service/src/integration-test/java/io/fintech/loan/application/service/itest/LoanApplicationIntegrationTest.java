package io.fintech.loan.application.service.itest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.notNullValue;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.time.Duration;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

@DisplayName("LoanApplication end-to-end integration tests")
class LoanApplicationIntegrationTest extends IntegrationTest {

  @Autowired
  private DataSource dataSource;

  private JdbcTemplate jdbc;

  @BeforeEach
  void setUp() {
    RestAssured.port = port;
    jdbc = new JdbcTemplate(dataSource);
    jdbc.execute("TRUNCATE TABLE camelbee_user.loan_applications");
  }

  @Test
  @DisplayName("Submit: POST /loan-applications returns 202 RECEIVED and persists")
  void test_Submit_Success() {
    String body = """
        {
          "applicantId": "APP-IT-001",
          "applicantName": "Integration Test User",
          "applicantEmail": "it@example.com",
          "requestedAmount": 25000.00,
          "purpose": "PERSONAL",
          "termMonths": 36,
          "monthlyIncome": 5000.00,
          "creditScore": 720,
          "employmentStatus": "EMPLOYED"
        }
        """;

    Response response = RestAssured.given()
        .contentType(ContentType.JSON)
        .header("transactionId", "11111111-1111-1111-1111-111111111111")
        .header("requestId", "22222222-2222-2222-2222-222222222222")
        .body(body)
        .post("/camelbee-service/loan-applications");

    response.then()
        .statusCode(202)
        .body("applicationId", notNullValue())
        .body("status", org.hamcrest.Matchers.equalTo("RECEIVED"));

    // DB row persisted
    Integer count = jdbc.queryForObject(
        "SELECT COUNT(*) FROM camelbee_user.loan_applications WHERE applicant_id = ?",
        Integer.class, "APP-IT-001");
    assertThat(count).isEqualTo(1);
  }

  @Test
  @DisplayName("Submit: POST with missing required field returns 400")
  void test_Submit_ValidationError() {
    String body = """
        {
          "applicantName": "Missing applicantId",
          "applicantEmail": "it@example.com",
          "requestedAmount": 1000.00,
          "purpose": "PERSONAL",
          "termMonths": 12,
          "monthlyIncome": 3000.00,
          "creditScore": 700,
          "employmentStatus": "EMPLOYED"
        }
        """;

    RestAssured.given()
        .contentType(ContentType.JSON)
        .header("transactionId", "11111111-1111-1111-1111-111111111111")
        .header("requestId", "22222222-2222-2222-2222-222222222222")
        .body(body)
        .post("/camelbee-service/loan-applications")
        .then()
        .statusCode(400);
  }

  @Test
  @DisplayName("Get: submit then GET /loan-applications/{id} returns full application")
  void test_Submit_And_Get_Success() {
    String submitBody = """
        {
          "applicantId": "APP-IT-002",
          "applicantName": "Get Test",
          "applicantEmail": "get@example.com",
          "requestedAmount": 4000.00,
          "purpose": "PERSONAL",
          "termMonths": 24,
          "monthlyIncome": 4000.00,
          "creditScore": 750,
          "employmentStatus": "EMPLOYED"
        }
        """;

    String applicationId = RestAssured.given()
        .contentType(ContentType.JSON)
        .header("transactionId", "33333333-3333-3333-3333-333333333333")
        .header("requestId", "44444444-4444-4444-4444-444444444444")
        .body(submitBody)
        .post("/camelbee-service/loan-applications")
        .then().statusCode(202)
        .extract().path("applicationId");

    // GET should return the application (Auto-Approve path eventually updates it,
    // but the initial RECEIVED state is also valid)
    await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> RestAssured.given()
        .header("transactionId", "55555555-5555-5555-5555-555555555555")
        .header("requestId", "66666666-6666-6666-6666-666666666666")
        .get("/camelbee-service/loan-applications/" + applicationId)
        .then()
        .statusCode(200)
        .body("applicationId", org.hamcrest.Matchers.equalTo(applicationId))
        .body("applicantId", org.hamcrest.Matchers.equalTo("APP-IT-002")));
  }

  @Test
  @DisplayName("Get: unknown applicationId returns 404")
  void test_Get_NotFound() {
    RestAssured.given()
        .header("transactionId", "77777777-7777-7777-7777-777777777777")
        .header("requestId", "88888888-8888-8888-8888-888888888888")
        .get("/camelbee-service/loan-applications/99999999-9999-9999-9999-999999999999")
        .then()
        .statusCode(404);
  }

  @Test
  @DisplayName("List: returns paginated applications (seeded by reset script)")
  void test_List_Success() {
    // Re-seed with a few rows so the list is non-empty.
    jdbc.update("INSERT INTO camelbee_user.loan_applications "
        + "(application_id, applicant_id, applicant_name, applicant_email, "
        + " requested_amount, purpose, term_months, monthly_income, credit_score, "
        + " employment_status, status, submitted_at) "
        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())",
        "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee", "APP-LIST-1", "User1", "u1@x.com",
        new java.math.BigDecimal("10000"), "PERSONAL", 24,
        new java.math.BigDecimal("4000"), 650, "EMPLOYED", "PENDING_REVIEW");

    RestAssured.given()
        .header("transactionId", "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
        .header("requestId", "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")
        .queryParam("status", "PENDING_REVIEW")
        .queryParam("page", 0)
        .queryParam("pageSize", 10)
        .get("/camelbee-service/loan-applications")
        .then()
        .statusCode(200)
        .body("applications", notNullValue())
        .body("totalItems", notNullValue());
  }

  @Test
  @DisplayName("Auto-Reject path: low creditScore → REJECTED via Kafka consumer")
  void test_Submit_AutoReject_AsyncProcessing() {
    String body = """
        {
          "applicantId": "APP-IT-REJECT",
          "applicantName": "Auto Reject",
          "applicantEmail": "reject@example.com",
          "requestedAmount": 8000.00,
          "purpose": "PERSONAL",
          "termMonths": 36,
          "monthlyIncome": 3000.00,
          "creditScore": 450,
          "employmentStatus": "EMPLOYED"
        }
        """;

    String applicationId = RestAssured.given()
        .contentType(ContentType.JSON)
        .header("transactionId", "cccccccc-cccc-cccc-cccc-cccccccccccc")
        .header("requestId", "dddddddd-dddd-dddd-dddd-dddddddddddd")
        .body(body)
        .post("/camelbee-service/loan-applications")
        .then().statusCode(202)
        .extract().path("applicationId");

    // Kafka consumer should eventually mark this REJECTED
    await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
      String status = jdbc.queryForObject(
          "SELECT status FROM camelbee_user.loan_applications WHERE application_id = ?",
          String.class, applicationId);
      assertThat(status).isEqualTo("REJECTED");
    });
  }
}
