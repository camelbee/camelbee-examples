package io.fintech.loan.application.service.utils;

import io.fintech.loan.application.service.model.domain.ApplicationStatus;
import io.fintech.loan.application.service.model.domain.EmploymentStatus;
import io.fintech.loan.application.service.model.domain.LoanApplication;
import io.fintech.loan.application.service.model.domain.LoanPurpose;
import java.math.BigDecimal;
import java.time.Instant;

public final class TestDataFactory {

  private TestDataFactory() {
  }

  public static LoanApplication submissionInput() {
    return LoanApplication.builder()
        .applicantId("APP-001")
        .applicantName("Jane Doe")
        .applicantEmail("jane.doe@example.com")
        .requestedAmount(new BigDecimal("25000.00"))
        .purpose(LoanPurpose.PERSONAL)
        .termMonths(36)
        .monthlyIncome(new BigDecimal("5000.00"))
        .creditScore(720)
        .employmentStatus(EmploymentStatus.EMPLOYED)
        .build();
  }

  public static LoanApplication autoApproveInput() {
    LoanApplication app = submissionInput();
    app.setApplicationId("11111111-1111-1111-1111-111111111111");
    app.setRequestedAmount(new BigDecimal("3000.00"));
    app.setCreditScore(750);
    app.setStatus(ApplicationStatus.RECEIVED);
    app.setSubmittedAt(Instant.parse("2026-05-17T10:00:00Z"));
    return app;
  }

  public static LoanApplication autoRejectInput() {
    LoanApplication app = submissionInput();
    app.setApplicationId("22222222-2222-2222-2222-222222222222");
    app.setRequestedAmount(new BigDecimal("8000.00"));
    app.setCreditScore(450);
    app.setStatus(ApplicationStatus.RECEIVED);
    app.setSubmittedAt(Instant.parse("2026-05-17T10:00:00Z"));
    return app;
  }

  public static LoanApplication bureauApprovalInput() {
    LoanApplication app = submissionInput();
    app.setApplicationId("33333333-3333-3333-3333-333333333333");
    app.setRequestedAmount(new BigDecimal("20000.00"));
    app.setCreditScore(700);
    app.setStatus(ApplicationStatus.RECEIVED);
    app.setSubmittedAt(Instant.parse("2026-05-17T10:00:00Z"));
    return app;
  }

  public static LoanApplication bureauReviewInput() {
    LoanApplication app = submissionInput();
    app.setApplicationId("44444444-4444-4444-4444-444444444444");
    app.setRequestedAmount(new BigDecimal("15000.00"));
    app.setCreditScore(600);
    app.setStatus(ApplicationStatus.RECEIVED);
    app.setSubmittedAt(Instant.parse("2026-05-17T10:00:00Z"));
    return app;
  }
}
