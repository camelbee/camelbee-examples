package com.mycompany.product.catalog.mapper.infra;

import com.mycompany.product.catalog.config.SharedMapperConfig;
import com.mycompany.product.catalog.model.domain.AuditLog;
import com.mycompany.product.catalog.model.domain.ResponseStatus;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;

/**
 * Mapper for converting Domain AuditLog to JPA AuditLog entity and vice versa.
 */
@Mapper(config = SharedMapperConfig.class, collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface JpaAuditLogMapper {

  // Domain AuditLog to JPA AuditLog
  @Mapping(source = "timestamp", target = "timestampUtc")
  @Mapping(source = "responseStatus", target = "responseStatus")
  @Mapping(target = "id", ignore = true)
  com.mycompany.product.catalog.model.infra.jpa.postgresql.AuditLog domainToJpaAuditLog(AuditLog auditLog);

  // JPA AuditLog to Domain AuditLog
  @Mapping(source = "timestampUtc", target = "timestamp")
  @Mapping(source = "responseStatus", target = "responseStatus")
  AuditLog jpaTodomainAuditLog(com.mycompany.product.catalog.model.infra.jpa.postgresql.AuditLog auditLog);

  // Enum mappings
  default com.mycompany.product.catalog.model.infra.jpa.postgresql.AuditLog.ResponseStatusEnum mapStatus(ResponseStatus status) {
    if (status == null) {
      return null;
    }
    return switch (status) {
      case SUCCESS -> com.mycompany.product.catalog.model.infra.jpa.postgresql.AuditLog.ResponseStatusEnum.SUCCESS;
      case FAILURE -> com.mycompany.product.catalog.model.infra.jpa.postgresql.AuditLog.ResponseStatusEnum.FAILURE;
    };
  }

  default ResponseStatus mapStatus(com.mycompany.product.catalog.model.infra.jpa.postgresql.AuditLog.ResponseStatusEnum status) {
    if (status == null) {
      return null;
    }
    return switch (status) {
      case SUCCESS -> ResponseStatus.SUCCESS;
      case FAILURE -> ResponseStatus.FAILURE;
    };
  }

}
