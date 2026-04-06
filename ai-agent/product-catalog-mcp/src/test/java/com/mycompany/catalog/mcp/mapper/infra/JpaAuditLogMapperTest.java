package com.mycompany.catalog.mcp.mapper.infra;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.mycompany.catalog.mcp.model.domain.AuditLog;
import com.mycompany.catalog.mcp.model.infra.jpa.postgresql.AuditLogEntity;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

@DisplayName("JpaAuditLogMapper Tests")
class JpaAuditLogMapperTest {

  private JpaAuditLogMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = Mappers.getMapper(JpaAuditLogMapper.class);
  }

  @Test
  @DisplayName("Should map Domain AuditLog to JPA AuditLogEntity")
  void domainAuditLogToJpaEntity_shouldMapAllFields() {
    Instant now = Instant.now();
    AuditLog domain = AuditLog.builder()
        .userId("user-123")
        .toolName("listProducts")
        .parameters("{\"page\":1}")
        .timestamp(now)
        .responseStatus(AuditLog.ResponseStatus.SUCCESS)
        .build();

    AuditLogEntity result = mapper.domainAuditLogToJpaAuditLogEntity(domain);

    assertNotNull(result);
    assertNull(result.getId()); // ID should be ignored (auto-generated)
    assertEquals("user-123", result.getUserId());
    assertEquals("listProducts", result.getToolName());
    assertEquals("{\"page\":1}", result.getParameters());
    assertEquals(now, result.getTimestamp());
    assertEquals(AuditLogEntity.ResponseStatus.SUCCESS, result.getResponseStatus());
  }

  @Test
  @DisplayName("Should map JPA AuditLogEntity to Domain AuditLog")
  void jpaEntityToDomainAuditLog_shouldMapAllFields() {
    Instant now = Instant.now();
    AuditLogEntity entity = new AuditLogEntity();
    entity.setId(42L);
    entity.setUserId("user-456");
    entity.setToolName("getProduct");
    entity.setParameters("{\"productId\":\"prod-001\"}");
    entity.setTimestamp(now);
    entity.setResponseStatus(AuditLogEntity.ResponseStatus.FAILURE);

    AuditLog result = mapper.jpaAuditLogEntityToDomainAuditLog(entity);

    assertNotNull(result);
    assertEquals(42L, result.getId());
    assertEquals("user-456", result.getUserId());
    assertEquals("getProduct", result.getToolName());
    assertEquals("{\"productId\":\"prod-001\"}", result.getParameters());
    assertEquals(now, result.getTimestamp());
    assertEquals(AuditLog.ResponseStatus.FAILURE, result.getResponseStatus());
  }

  @Test
  @DisplayName("Should handle null input")
  void nullInput_shouldReturnNull() {
    assertNull(mapper.domainAuditLogToJpaAuditLogEntity(null));
    assertNull(mapper.jpaAuditLogEntityToDomainAuditLog(null));
  }

}
