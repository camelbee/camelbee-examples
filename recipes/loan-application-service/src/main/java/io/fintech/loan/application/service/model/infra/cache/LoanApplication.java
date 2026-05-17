package io.fintech.loan.application.service.model.infra.cache;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoanApplication {

  private String applicationId;
  private String applicantId;
  private String applicantName;
  private String applicantEmail;
  private BigDecimal requestedAmount;
  private String purpose;
  private Integer termMonths;
  private BigDecimal monthlyIncome;
  private Integer creditScore;
  private String employmentStatus;
  private String status;
  private Integer riskScore;
  private String decisionReason;
  private Instant submittedAt;
  private Instant processedAt;
}
