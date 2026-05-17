package io.fintech.loan.application.service.model.infra.jpa.postgresql;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "LOAN_APPLICATIONS")
@NamedQueries({
    @NamedQuery(
        name = "LoanApplication.findByApplicationId",
        query = "SELECT a FROM LoanApplication a WHERE a.applicationId = :applicationId"
    ),
    @NamedQuery(
        name = "LoanApplication.findAll",
        query = "SELECT a FROM LoanApplication a ORDER BY a.id"
    ),
    @NamedQuery(
        name = "LoanApplication.findByStatus",
        query = "SELECT a FROM LoanApplication a WHERE a.status = :status ORDER BY a.id"
    ),
    @NamedQuery(
        name = "LoanApplication.countAll",
        query = "SELECT COUNT(a) FROM LoanApplication a"
    ),
    @NamedQuery(
        name = "LoanApplication.countByStatus",
        query = "SELECT COUNT(a) FROM LoanApplication a WHERE a.status = :status"
    )
})
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class LoanApplication {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "application_id", unique = true, nullable = false, length = 36)
  private String applicationId;

  @Column(name = "applicant_id", length = 100)
  private String applicantId;

  @Column(name = "applicant_name", length = 200)
  private String applicantName;

  @Column(name = "applicant_email", length = 200)
  private String applicantEmail;

  @Column(name = "requested_amount", precision = 15, scale = 2)
  private BigDecimal requestedAmount;

  @Column(name = "purpose", length = 50)
  private String purpose;

  @Column(name = "term_months")
  private Integer termMonths;

  @Column(name = "monthly_income", precision = 15, scale = 2)
  private BigDecimal monthlyIncome;

  @Column(name = "credit_score")
  private Integer creditScore;

  @Column(name = "employment_status", length = 50)
  private String employmentStatus;

  @Column(name = "status", length = 20)
  private String status;

  @Column(name = "risk_score")
  private Integer riskScore;

  @Column(name = "decision_reason", length = 500)
  private String decisionReason;

  @Column(name = "submitted_at")
  private Instant submittedAt;

  @Column(name = "processed_at")
  private Instant processedAt;
}
