package io.fintech.loan.application.service.mapper.api;

import static org.assertj.core.api.Assertions.assertThat;

import io.fintech.loan.application.service.model.api.avro.LoanApplicationProcessedEvent;
import io.fintech.loan.application.service.model.api.avro.LoanApplicationSubmittedEvent;
import io.fintech.loan.application.service.model.domain.ApplicationStatus;
import io.fintech.loan.application.service.model.domain.LoanApplication;
import io.fintech.loan.application.service.utils.TestDataFactory;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class AvroLoanApplicationEventMapperTest {

  private AvroLoanApplicationEventMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = Mappers.getMapper(AvroLoanApplicationEventMapper.class);
  }

  @Test
  @DisplayName("Maps domain to LoanApplicationSubmittedEvent")
  void test_domainToSubmittedEvent_Success() {
    LoanApplication app = TestDataFactory.autoApproveInput();

    LoanApplicationSubmittedEvent event = mapper.domainToSubmittedEvent(app);

    assertThat(event.getApplicationId().toString()).isEqualTo(app.getApplicationId());
    assertThat(event.getRequestedAmount()).isEqualTo(app.getRequestedAmount().doubleValue());
    assertThat(event.getPurpose().toString()).isEqualTo("PERSONAL");
    assertThat(event.getEmploymentStatus().toString()).isEqualTo("EMPLOYED");
    assertThat(event.getCreditScore()).isEqualTo(app.getCreditScore());
  }

  @Test
  @DisplayName("Maps domain to LoanApplicationProcessedEvent")
  void test_domainToProcessedEvent_Success() {
    LoanApplication app = TestDataFactory.autoApproveInput();
    app.setStatus(ApplicationStatus.APPROVED);
    app.setRiskScore(10);
    app.setDecisionReason("Auto-approved: low-risk application");
    Instant processed = Instant.parse("2026-05-17T12:30:00Z");
    app.setProcessedAt(processed);

    LoanApplicationProcessedEvent event = mapper.domainToProcessedEvent(app);

    assertThat(event.getApplicationId().toString()).isEqualTo(app.getApplicationId());
    assertThat(event.getDecision().toString()).isEqualTo("APPROVED");
    assertThat(event.getRiskScore()).isEqualTo(10);
    assertThat(event.getReason().toString()).isEqualTo("Auto-approved: low-risk application");
    assertThat(event.getProcessedAt()).isEqualTo(processed);
  }

  @Test
  @DisplayName("Maps SubmittedEvent back to domain")
  void test_submittedEventToDomain_Success() {
    LoanApplicationSubmittedEvent event = mapper.domainToSubmittedEvent(TestDataFactory.autoApproveInput());

    LoanApplication domain = mapper.submittedEventToDomain(event);

    assertThat(domain.getApplicationId()).isEqualTo("11111111-1111-1111-1111-111111111111");
    assertThat(domain.getRequestedAmount()).isEqualByComparingTo("3000.00");
    assertThat(domain.getCreditScore()).isEqualTo(750);
  }

  @Test
  @DisplayName("Null handling")
  void test_NullHandling() {
    assertThat(mapper.domainToSubmittedEvent(null)).isNull();
    assertThat(mapper.domainToProcessedEvent(null)).isNull();
    assertThat(mapper.submittedEventToDomain(null)).isNull();
    assertThat(mapper.processedEventToDomain(null)).isNull();
  }
}
