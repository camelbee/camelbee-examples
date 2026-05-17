package io.fintech.loan.application.service.mapper.api;

import io.fintech.loan.application.service.config.SharedMapperConfig;
import io.fintech.loan.application.service.model.api.avro.LoanApplicationProcessedEvent;
import io.fintech.loan.application.service.model.api.avro.LoanApplicationSubmittedEvent;
import io.fintech.loan.application.service.model.domain.ApplicationStatus;
import io.fintech.loan.application.service.model.domain.LoanApplication;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(config = SharedMapperConfig.class, collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface AvroLoanApplicationEventMapper {

  @Mapping(target = "requestedAmount", expression = "java(application.getRequestedAmount() == null ? 0.0 : application.getRequestedAmount().doubleValue())")
  @Mapping(target = "monthlyIncome", expression = "java(application.getMonthlyIncome() == null ? 0.0 : application.getMonthlyIncome().doubleValue())")
  @Mapping(target = "purpose", expression = "java(application.getPurpose() == null ? null : application.getPurpose().name())")
  @Mapping(target = "employmentStatus", expression = "java(application.getEmploymentStatus() == null ? null : application.getEmploymentStatus().name())")
  LoanApplicationSubmittedEvent domainToSubmittedEvent(LoanApplication application);

  @Mapping(target = "decision", expression = "java(application.getStatus() == null ? null : application.getStatus().name())")
  @Mapping(target = "reason", source = "decisionReason")
  LoanApplicationProcessedEvent domainToProcessedEvent(LoanApplication application);

  default LoanApplication submittedEventToDomain(LoanApplicationSubmittedEvent event) {
    if (event == null) {
      return null;
    }
    return LoanApplication.builder()
        .applicationId(charSeqToString(event.getApplicationId()))
        .applicantId(charSeqToString(event.getApplicantId()))
        .applicantName(charSeqToString(event.getApplicantName()))
        .applicantEmail(charSeqToString(event.getApplicantEmail()))
        .requestedAmount(java.math.BigDecimal.valueOf(event.getRequestedAmount()))
        .purpose(parsePurpose(event.getPurpose()))
        .termMonths(event.getTermMonths())
        .monthlyIncome(java.math.BigDecimal.valueOf(event.getMonthlyIncome()))
        .creditScore(event.getCreditScore())
        .employmentStatus(parseEmployment(event.getEmploymentStatus()))
        .submittedAt(event.getSubmittedAt())
        .build();
  }

  default LoanApplication processedEventToDomain(LoanApplicationProcessedEvent event) {
    if (event == null) {
      return null;
    }
    return LoanApplication.builder()
        .applicationId(charSeqToString(event.getApplicationId()))
        .applicantId(charSeqToString(event.getApplicantId()))
        .status(parseStatus(event.getDecision()))
        .riskScore(event.getRiskScore())
        .decisionReason(charSeqToString(event.getReason()))
        .processedAt(event.getProcessedAt())
        .build();
  }

  default String charSeqToString(CharSequence value) {
    return value == null ? null : value.toString();
  }

  default io.fintech.loan.application.service.model.domain.LoanPurpose parsePurpose(CharSequence value) {
    return value == null ? null : io.fintech.loan.application.service.model.domain.LoanPurpose.valueOf(value.toString());
  }

  default io.fintech.loan.application.service.model.domain.EmploymentStatus parseEmployment(CharSequence value) {
    return value == null ? null : io.fintech.loan.application.service.model.domain.EmploymentStatus.valueOf(value.toString());
  }

  default ApplicationStatus parseStatus(CharSequence value) {
    return value == null ? null : ApplicationStatus.valueOf(value.toString());
  }
}
