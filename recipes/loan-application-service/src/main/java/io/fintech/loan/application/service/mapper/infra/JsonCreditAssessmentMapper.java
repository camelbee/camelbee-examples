package io.fintech.loan.application.service.mapper.infra;

import io.fintech.loan.application.service.config.SharedMapperConfig;
import io.fintech.loan.application.service.model.domain.EmploymentStatus;
import io.fintech.loan.application.service.model.domain.LoanApplication;
import io.fintech.loan.application.service.model.infra.json.CreditAssessmentRequest;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(config = SharedMapperConfig.class, collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface JsonCreditAssessmentMapper {

  @Mapping(source = "applicantId", target = "applicantId")
  @Mapping(source = "requestedAmount", target = "requestedAmount")
  @Mapping(source = "creditScore", target = "creditScore")
  @Mapping(source = "monthlyIncome", target = "monthlyIncome")
  @Mapping(source = "employmentStatus", target = "employmentStatus")
  CreditAssessmentRequest domainToCreditAssessmentRequest(LoanApplication application);

  default io.fintech.loan.application.service.model.infra.json.EmploymentStatus mapEmployment(EmploymentStatus value) {
    return value == null ? null
        : io.fintech.loan.application.service.model.infra.json.EmploymentStatus.valueOf(value.name());
  }

  default EmploymentStatus mapEmployment(io.fintech.loan.application.service.model.infra.json.EmploymentStatus value) {
    return value == null ? null : EmploymentStatus.valueOf(value.name());
  }
}
