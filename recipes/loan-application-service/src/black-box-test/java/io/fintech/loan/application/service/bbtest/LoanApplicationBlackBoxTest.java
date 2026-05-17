package io.fintech.loan.application.service.bbtest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

@DisplayName("Loan Application black-box tests (fully dockerized end-to-end)")
class LoanApplicationBlackBoxTest extends BlackBoxTest {

  @BeforeEach
  void clean() throws Exception {
    truncateLoanApplications();
  }

  @Test
  @Order(1)
  @DisplayName("Health endpoint responds 200")
  void test_Health() {
    RestAssured.given().get("/health").then().statusCode(200);
  }

  @Test
  @Order(2)
  @DisplayName("Submit: returns 202 RECEIVED, persists row to Postgres")
  void test_Submit_Success() throws Exception {
    String body = """
        {
          "applicantId": "BBT-001",
          "applicantName": "BBT User",
          "applicantEmail": "bbt@example.com",
          "requestedAmount": 25000.00,
          "purpose": "PERSONAL",
          "termMonths": 36,
          "monthlyIncome": 5000.00,
          "creditScore": 720,
          "employmentStatus": "EMPLOYED"
        }
        """;

    RestAssured.given()
        .contentType(ContentType.JSON)
        .header("transactionId", "11111111-1111-1111-1111-111111111111")
        .header("requestId", "22222222-2222-2222-2222-222222222222")
        .body(body)
        .post("/camelbee-service/loan-applications")
        .then().statusCode(202)
        .body("applicationId", notNullValue())
        .body("status", equalTo("RECEIVED"));

    assertThat(countByApplicantId("BBT-001")).isEqualTo(1);
  }

  @Test
  @Order(3)
  @DisplayName("Submit: missing applicantId returns 400")
  void test_Submit_ValidationError() {
    RestAssured.given()
        .contentType(ContentType.JSON)
        .header("transactionId", "11111111-1111-1111-1111-111111111111")
        .header("requestId", "33333333-3333-3333-3333-333333333333")
        .body("""
            {
              "applicantName": "no id",
              "applicantEmail": "x@x.com",
              "requestedAmount": 500.00,
              "purpose": "PERSONAL",
              "termMonths": 12,
              "monthlyIncome": 3000.00,
              "creditScore": 700,
              "employmentStatus": "EMPLOYED"
            }
            """)
        .post("/camelbee-service/loan-applications")
        .then().statusCode(400);
  }

  @Test
  @Order(4)
  @DisplayName("Get: round-trip submit then GET returns 200")
  void test_Submit_And_Get_Success() {
    String applicationId = RestAssured.given()
        .contentType(ContentType.JSON)
        .header("transactionId", "11111111-1111-1111-1111-111111111111")
        .header("requestId", "44444444-4444-4444-4444-444444444444")
        .body("""
            {
              "applicantId": "BBT-002",
              "applicantName": "Round Trip",
              "applicantEmail": "rt@example.com",
              "requestedAmount": 4000.00,
              "purpose": "PERSONAL",
              "termMonths": 24,
              "monthlyIncome": 4500.00,
              "creditScore": 750,
              "employmentStatus": "EMPLOYED"
            }
            """)
        .post("/camelbee-service/loan-applications")
        .then().statusCode(202)
        .extract().path("applicationId");

    await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> RestAssured.given()
        .header("transactionId", "11111111-1111-1111-1111-111111111111")
        .header("requestId", "55555555-5555-5555-5555-555555555555")
        .get("/camelbee-service/loan-applications/" + applicationId)
        .then().statusCode(200)
        .body("applicationId", equalTo(applicationId))
        .body("applicantId", equalTo("BBT-002")));
  }

  @Test
  @Order(5)
  @DisplayName("Get: unknown applicationId returns 404")
  void test_Get_NotFound() {
    RestAssured.given()
        .header("transactionId", "11111111-1111-1111-1111-111111111111")
        .header("requestId", "66666666-6666-6666-6666-666666666666")
        .get("/camelbee-service/loan-applications/00000000-0000-0000-0000-000000000000")
        .then().statusCode(404);
  }

  @Test
  @Order(6)
  @DisplayName("Auto-Reject: submit with creditScore < 500 yields REJECTED via Kafka consumer")
  void test_Submit_AutoReject_Async() throws Exception {
    String applicationId = RestAssured.given()
        .contentType(ContentType.JSON)
        .header("transactionId", "11111111-1111-1111-1111-111111111111")
        .header("requestId", "77777777-7777-7777-7777-777777777777")
        .body("""
            {
              "applicantId": "BBT-REJECT",
              "applicantName": "Auto Reject",
              "applicantEmail": "rj@example.com",
              "requestedAmount": 8000.00,
              "purpose": "PERSONAL",
              "termMonths": 36,
              "monthlyIncome": 3000.00,
              "creditScore": 450,
              "employmentStatus": "EMPLOYED"
            }
            """)
        .post("/camelbee-service/loan-applications")
        .then().statusCode(202)
        .extract().path("applicationId");

    await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> assertThat(statusByApplicationId(applicationId)).isEqualTo("REJECTED"));
  }
}
