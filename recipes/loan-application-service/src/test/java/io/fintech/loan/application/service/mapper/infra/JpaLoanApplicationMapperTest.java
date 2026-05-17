package io.fintech.loan.application.service.mapper.infra;

import static org.assertj.core.api.Assertions.assertThat;

import io.fintech.loan.application.service.model.domain.ApplicationStatus;
import io.fintech.loan.application.service.model.domain.EmploymentStatus;
import io.fintech.loan.application.service.model.domain.LoanApplication;
import io.fintech.loan.application.service.model.domain.LoanPurpose;
import io.fintech.loan.application.service.utils.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class JpaLoanApplicationMapperTest {

  private JpaLoanApplicationMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = Mappers.getMapper(JpaLoanApplicationMapper.class);
  }

  @Test
  @DisplayName("Maps domain LoanApplication to JPA entity (enums as strings)")
  void test_domainToJpa_Success() {
    LoanApplication app = TestDataFactory.bureauApprovalInput();
    app.setStatus(ApplicationStatus.APPROVED);

    var entity = mapper.domainToJpa(app);

    assertThat(entity.getApplicationId()).isEqualTo(app.getApplicationId());
    assertThat(entity.getStatus()).isEqualTo("APPROVED");
    assertThat(entity.getPurpose()).isEqualTo("PERSONAL");
    assertThat(entity.getEmploymentStatus()).isEqualTo("EMPLOYED");
  }

  @Test
  @DisplayName("Maps JPA entity to domain (strings as enums)")
  void test_jpaToDomain_Success() {
    var entity = new io.fintech.loan.application.service.model.infra.jpa.postgresql.LoanApplication();
    entity.setApplicationId("xyz");
    entity.setStatus("PENDING_REVIEW");
    entity.setPurpose("HOME_PURCHASE");
    entity.setEmploymentStatus("SELF_EMPLOYED");
    entity.setCreditScore(620);

    LoanApplication domain = mapper.jpaToDomain(entity);

    assertThat(domain.getApplicationId()).isEqualTo("xyz");
    assertThat(domain.getStatus()).isEqualTo(ApplicationStatus.PENDING_REVIEW);
    assertThat(domain.getPurpose()).isEqualTo(LoanPurpose.HOME_PURCHASE);
    assertThat(domain.getEmploymentStatus()).isEqualTo(EmploymentStatus.SELF_EMPLOYED);
    assertThat(domain.getCreditScore()).isEqualTo(620);
  }

  @Test
  @DisplayName("Null inputs return null")
  void test_NullHandling() {
    assertThat(mapper.domainToJpa(null)).isNull();
    assertThat(mapper.jpaToDomain(null)).isNull();
  }
}
