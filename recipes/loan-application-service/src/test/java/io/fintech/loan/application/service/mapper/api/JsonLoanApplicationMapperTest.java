package io.fintech.loan.application.service.mapper.api;

import static org.assertj.core.api.Assertions.assertThat;

import io.fintech.loan.application.service.model.api.json.LoanApplicationSubmissionRequest;
import io.fintech.loan.application.service.model.domain.ApplicationStatus;
import io.fintech.loan.application.service.model.domain.EmploymentStatus;
import io.fintech.loan.application.service.model.domain.LoanApplication;
import io.fintech.loan.application.service.model.domain.LoanPurpose;
import io.fintech.loan.application.service.utils.TestDataFactory;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class JsonLoanApplicationMapperTest {

  private JsonLoanApplicationMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = Mappers.getMapper(JsonLoanApplicationMapper.class);
  }

  @Test
  @DisplayName("Maps JSON submission request to domain")
  void test_jsonRequestToDomain_Success() {
    LoanApplicationSubmissionRequest req = new LoanApplicationSubmissionRequest();
    req.setApplicantId("APP-1");
    req.setApplicantName("Jane");
    req.setApplicantEmail("jane@example.com");
    req.setRequestedAmount(new BigDecimal("1234.56"));
    req.setPurpose(io.fintech.loan.application.service.model.api.json.LoanPurpose.PERSONAL);
    req.setTermMonths(36);
    req.setMonthlyIncome(new BigDecimal("5000"));
    req.setCreditScore(700);
    req.setEmploymentStatus(io.fintech.loan.application.service.model.api.json.EmploymentStatus.EMPLOYED);

    LoanApplication domain = mapper.jsonRequestToDomain(req);

    assertThat(domain.getApplicantId()).isEqualTo("APP-1");
    assertThat(domain.getRequestedAmount()).isEqualByComparingTo("1234.56");
    assertThat(domain.getPurpose()).isEqualTo(LoanPurpose.PERSONAL);
    assertThat(domain.getEmploymentStatus()).isEqualTo(EmploymentStatus.EMPLOYED);
    assertThat(domain.getTermMonths()).isEqualTo(36);
  }

  @Test
  @DisplayName("Maps domain to JSON submission response (3 fields)")
  void test_domainToJsonSubmissionResponse_Success() {
    LoanApplication app = TestDataFactory.submissionInput();
    String applicationId = UUID.randomUUID().toString();
    app.setApplicationId(applicationId);
    app.setStatus(ApplicationStatus.RECEIVED);
    Instant now = Instant.parse("2026-05-17T12:00:00Z");
    app.setSubmittedAt(now);

    var response = mapper.domainToJsonSubmissionResponse(app);

    assertThat(response.getApplicationId().toString()).isEqualTo(applicationId);
    assertThat(response.getStatus())
        .isEqualTo(io.fintech.loan.application.service.model.api.json.ApplicationStatus.RECEIVED);
    assertThat(response.getSubmittedAt().toInstant()).isEqualTo(now);
  }

  @Test
  @DisplayName("Maps domain to full JSON LoanApplication including nullable fields")
  void test_domainToJsonLoanApplication_Success() {
    LoanApplication app = TestDataFactory.bureauApprovalInput();
    app.setStatus(ApplicationStatus.APPROVED);
    app.setRiskScore(25);
    app.setDecisionReason("Credit profile acceptable");
    Instant processed = Instant.parse("2026-05-17T12:30:00Z");
    app.setProcessedAt(processed);

    var json = mapper.domainToJsonLoanApplication(app);

    assertThat(json.getApplicationId().toString()).isEqualTo(app.getApplicationId());
    assertThat(json.getStatus())
        .isEqualTo(io.fintech.loan.application.service.model.api.json.ApplicationStatus.APPROVED);
    assertThat(json.getRiskScore()).isEqualTo(25);
    assertThat(json.getDecisionReason()).isEqualTo("Credit profile acceptable");
    assertThat(json.getProcessedAt().toInstant()).isEqualTo(processed);
  }

  @Test
  @DisplayName("toJsonPage wraps applications + pagination metadata")
  void test_toJsonPage_Success() {
    LoanApplication a = TestDataFactory.bureauApprovalInput();
    LoanApplication b = TestDataFactory.bureauReviewInput();

    var page = mapper.toJsonPage(List.of(a, b), 2, 0, 10);

    assertThat(page.getApplications()).hasSize(2);
    assertThat(page.getTotalItems()).isEqualTo(2);
    assertThat(page.getPage()).isEqualTo(0);
    assertThat(page.getPageSize()).isEqualTo(10);
  }

  @Test
  @DisplayName("Maps null safely")
  void test_NullHandling() {
    assertThat(mapper.jsonRequestToDomain(null)).isNull();
    assertThat(mapper.domainToJsonSubmissionResponse(null)).isNull();
    assertThat(mapper.domainToJsonLoanApplication(null)).isNull();
  }
}
