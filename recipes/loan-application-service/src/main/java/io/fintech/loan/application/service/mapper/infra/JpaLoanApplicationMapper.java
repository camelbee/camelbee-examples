package io.fintech.loan.application.service.mapper.infra;

import io.fintech.loan.application.service.config.SharedMapperConfig;
import io.fintech.loan.application.service.model.domain.ApplicationStatus;
import io.fintech.loan.application.service.model.domain.EmploymentStatus;
import io.fintech.loan.application.service.model.domain.LoanApplication;
import io.fintech.loan.application.service.model.domain.LoanPurpose;
import java.util.List;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(config = SharedMapperConfig.class, collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface JpaLoanApplicationMapper {

  @Mapping(target = "id", ignore = true)
  io.fintech.loan.application.service.model.infra.jpa.postgresql.LoanApplication domainToJpa(LoanApplication application);

  LoanApplication jpaToDomain(io.fintech.loan.application.service.model.infra.jpa.postgresql.LoanApplication entity);

  List<LoanApplication> jpaToDomainList(
      List<io.fintech.loan.application.service.model.infra.jpa.postgresql.LoanApplication> entities);

  default String map(LoanPurpose value) {
    return value == null ? null : value.name();
  }

  default LoanPurpose toLoanPurpose(String value) {
    return value == null ? null : LoanPurpose.valueOf(value);
  }

  default String map(EmploymentStatus value) {
    return value == null ? null : value.name();
  }

  default EmploymentStatus toEmploymentStatus(String value) {
    return value == null ? null : EmploymentStatus.valueOf(value);
  }

  default String map(ApplicationStatus value) {
    return value == null ? null : value.name();
  }

  default ApplicationStatus toApplicationStatus(String value) {
    return value == null ? null : ApplicationStatus.valueOf(value);
  }
}
