package com.mycompany.catalog.mcp.mapper.infra;

import com.mycompany.catalog.mcp.config.SharedMapperConfig;
import com.mycompany.catalog.mcp.model.domain.AuditLog;
import com.mycompany.catalog.mcp.model.infra.jpa.postgresql.AuditLogEntity;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;

/**
 * Mapper for converting Domain AuditLog to JPA AuditLogEntity and vice versa.
 */
@Mapper(config = SharedMapperConfig.class, collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface JpaAuditLogMapper {

  // Domain AuditLog to JPA AuditLogEntity
  @Mapping(target = "id", ignore = true)
  AuditLogEntity domainAuditLogToJpaAuditLogEntity(AuditLog auditLog);

  // JPA AuditLogEntity to Domain AuditLog
  AuditLog jpaAuditLogEntityToDomainAuditLog(AuditLogEntity entity);

}
