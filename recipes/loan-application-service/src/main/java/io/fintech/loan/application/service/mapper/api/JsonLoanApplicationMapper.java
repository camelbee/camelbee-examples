package io.fintech.loan.application.service.mapper.api;

import io.fintech.loan.application.service.config.SharedMapperConfig;
import io.fintech.loan.application.service.model.api.json.LoanApplicationPage;
import io.fintech.loan.application.service.model.api.json.LoanApplicationSubmissionRequest;
import io.fintech.loan.application.service.model.api.json.LoanApplicationSubmissionResponse;
import io.fintech.loan.application.service.model.domain.ApplicationStatus;
import io.fintech.loan.application.service.model.domain.EmploymentStatus;
import io.fintech.loan.application.service.model.domain.LoanApplication;
import io.fintech.loan.application.service.model.domain.LoanPurpose;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(config = SharedMapperConfig.class, collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface JsonLoanApplicationMapper {

  // POST request → Domain (only input fields)
  LoanApplication jsonRequestToDomain(LoanApplicationSubmissionRequest request);

  // Domain → POST 202 response (3 fields)
  LoanApplicationSubmissionResponse domainToJsonSubmissionResponse(LoanApplication application);

  // Domain → GET full response
  io.fintech.loan.application.service.model.api.json.LoanApplication domainToJsonLoanApplication(LoanApplication application);

  // GET response → Domain (used in tests / round-trips)
  LoanApplication jsonLoanApplicationToDomain(io.fintech.loan.application.service.model.api.json.LoanApplication application);

  List<io.fintech.loan.application.service.model.api.json.LoanApplication> domainToJsonLoanApplications(
      List<LoanApplication> applications);

  default LoanApplicationPage toJsonPage(List<LoanApplication> applications, int totalItems, int page, int pageSize) {
    LoanApplicationPage p = new LoanApplicationPage();
    p.setApplications(domainToJsonLoanApplications(applications));
    p.setTotalItems(totalItems);
    p.setPage(page);
    p.setPageSize(pageSize);
    return p;
  }

  // ---- Type converters used by all the @Mapping above ----

  default String map(UUID value) {
    return value == null ? null : value.toString();
  }

  default UUID toUuid(String value) {
    return value == null ? null : UUID.fromString(value);
  }

  default OffsetDateTime map(Instant value) {
    return value == null ? null : value.atOffset(ZoneOffset.UTC);
  }

  default Instant map(OffsetDateTime value) {
    return value == null ? null : value.toInstant();
  }

  default io.fintech.loan.application.service.model.api.json.LoanPurpose mapPurpose(LoanPurpose value) {
    return value == null ? null : io.fintech.loan.application.service.model.api.json.LoanPurpose.valueOf(value.name());
  }

  default LoanPurpose mapPurpose(io.fintech.loan.application.service.model.api.json.LoanPurpose value) {
    return value == null ? null : LoanPurpose.valueOf(value.name());
  }

  default io.fintech.loan.application.service.model.api.json.EmploymentStatus mapEmployment(EmploymentStatus value) {
    return value == null ? null : io.fintech.loan.application.service.model.api.json.EmploymentStatus.valueOf(value.name());
  }

  default EmploymentStatus mapEmployment(io.fintech.loan.application.service.model.api.json.EmploymentStatus value) {
    return value == null ? null : EmploymentStatus.valueOf(value.name());
  }

  default io.fintech.loan.application.service.model.api.json.ApplicationStatus mapStatus(ApplicationStatus value) {
    return value == null ? null : io.fintech.loan.application.service.model.api.json.ApplicationStatus.valueOf(value.name());
  }

  default ApplicationStatus mapStatus(io.fintech.loan.application.service.model.api.json.ApplicationStatus value) {
    return value == null ? null : ApplicationStatus.valueOf(value.name());
  }
}
