package io.fintech.loan.application.service.model.domain;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LoanApplication {

  private String applicationId;
  private String applicantId;
  private String applicantName;
  private String applicantEmail;
  private BigDecimal requestedAmount;
  private LoanPurpose purpose;
  private Integer termMonths;
  private BigDecimal monthlyIncome;
  private Integer creditScore;
  private EmploymentStatus employmentStatus;
  private ApplicationStatus status;
  private Integer riskScore;
  private String decisionReason;
  private Instant submittedAt;
  private Instant processedAt;
}
