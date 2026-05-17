package io.fintech.loan.application.service.mapper.api;

import io.fintech.loan.application.service.config.SharedMapperConfig;
import io.fintech.loan.application.service.model.api.mcp.LoanApplicationPage;
import io.fintech.loan.application.service.model.api.mcp.LoanApplicationSubmissionRequest;
import io.fintech.loan.application.service.model.api.mcp.LoanApplicationSubmissionResponse;
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
public interface McpLoanApplicationMapper {

  LoanApplication mcpRequestToDomain(LoanApplicationSubmissionRequest request);

  LoanApplicationSubmissionResponse domainToMcpSubmissionResponse(LoanApplication application);

  io.fintech.loan.application.service.model.api.mcp.LoanApplication domainToMcpLoanApplication(LoanApplication application);

  LoanApplication mcpLoanApplicationToDomain(io.fintech.loan.application.service.model.api.mcp.LoanApplication application);

  List<io.fintech.loan.application.service.model.api.mcp.LoanApplication> domainToMcpLoanApplications(
      List<LoanApplication> applications);

  default LoanApplicationPage toMcpPage(List<LoanApplication> applications, int totalItems, int page, int pageSize) {
    LoanApplicationPage p = new LoanApplicationPage();
    p.setApplications(domainToMcpLoanApplications(applications));
    p.setTotalItems(totalItems);
    p.setPage(page);
    p.setPageSize(pageSize);
    return p;
  }

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

  default io.fintech.loan.application.service.model.api.mcp.LoanPurpose mapPurpose(LoanPurpose value) {
    return value == null ? null : io.fintech.loan.application.service.model.api.mcp.LoanPurpose.valueOf(value.name());
  }

  default LoanPurpose mapPurpose(io.fintech.loan.application.service.model.api.mcp.LoanPurpose value) {
    return value == null ? null : LoanPurpose.valueOf(value.name());
  }

  default io.fintech.loan.application.service.model.api.mcp.EmploymentStatus mapEmployment(EmploymentStatus value) {
    return value == null ? null : io.fintech.loan.application.service.model.api.mcp.EmploymentStatus.valueOf(value.name());
  }

  default EmploymentStatus mapEmployment(io.fintech.loan.application.service.model.api.mcp.EmploymentStatus value) {
    return value == null ? null : EmploymentStatus.valueOf(value.name());
  }

  default io.fintech.loan.application.service.model.api.mcp.ApplicationStatus mapStatus(ApplicationStatus value) {
    return value == null ? null : io.fintech.loan.application.service.model.api.mcp.ApplicationStatus.valueOf(value.name());
  }

  default ApplicationStatus mapStatus(io.fintech.loan.application.service.model.api.mcp.ApplicationStatus value) {
    return value == null ? null : ApplicationStatus.valueOf(value.name());
  }
}
