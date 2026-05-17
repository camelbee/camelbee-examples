package com.mycompany.product.catalog.mapper.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.mycompany.product.catalog.model.domain.AuditLog;
import com.mycompany.product.catalog.model.domain.ResponseStatus;
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
  @DisplayName("Should map Domain AuditLog to JPA AuditLog")
  void test_DomainToJpa_AuditLog() {
    Instant now = Instant.now();
    AuditLog domain = AuditLog.builder()
        .userId("user-123")
        .toolName("listProducts")
        .parameters("{\"page\":1}")
        .timestamp(now)
        .responseStatus(ResponseStatus.SUCCESS)
        .build();

    var jpa = mapper.domainToJpaAuditLog(domain);

    assertThat(jpa).isNotNull();
    assertThat(jpa.getId()).isNull(); // auto-generated, should be ignored
    assertThat(jpa.getUserId()).isEqualTo("user-123");
    assertThat(jpa.getToolName()).isEqualTo("listProducts");
    assertThat(jpa.getParameters()).isEqualTo("{\"page\":1}");
    assertThat(jpa.getTimestampUtc()).isEqualTo(now);
    assertThat(jpa.getResponseStatus()).isEqualTo(
        com.mycompany.product.catalog.model.infra.jpa.postgresql.AuditLog.ResponseStatusEnum.SUCCESS);
  }

  @Test
  @DisplayName("Should map JPA AuditLog to Domain AuditLog")
  void test_JpaToDomain_AuditLog() {
    Instant now = Instant.now();
    var jpa = new com.mycompany.product.catalog.model.infra.jpa.postgresql.AuditLog();
    jpa.setId(42L);
    jpa.setUserId("user-456");
    jpa.setToolName("getProduct");
    jpa.setParameters("{\"id\":\"prod-001\"}");
    jpa.setTimestampUtc(now);
    jpa.setResponseStatus(
        com.mycompany.product.catalog.model.infra.jpa.postgresql.AuditLog.ResponseStatusEnum.FAILURE);

    AuditLog domain = mapper.jpaTodomainAuditLog(jpa);

    assertThat(domain).isNotNull();
    assertThat(domain.getId()).isEqualTo(42L);
    assertThat(domain.getUserId()).isEqualTo("user-456");
    assertThat(domain.getToolName()).isEqualTo("getProduct");
    assertThat(domain.getParameters()).isEqualTo("{\"id\":\"prod-001\"}");
    assertThat(domain.getTimestamp()).isEqualTo(now);
    assertThat(domain.getResponseStatus()).isEqualTo(ResponseStatus.FAILURE);
  }

  @Test
  @DisplayName("Should handle null input")
  void test_NullInput() {
    assertThat(mapper.domainToJpaAuditLog(null)).isNull();
    assertThat(mapper.jpaTodomainAuditLog(null)).isNull();
  }

  @Test
  @DisplayName("Should map SUCCESS enum correctly")
  void test_MapStatus_Success() {
    assertThat(mapper.mapStatus(ResponseStatus.SUCCESS)).isEqualTo(
        com.mycompany.product.catalog.model.infra.jpa.postgresql.AuditLog.ResponseStatusEnum.SUCCESS);
    assertThat(mapper.mapStatus(
        com.mycompany.product.catalog.model.infra.jpa.postgresql.AuditLog.ResponseStatusEnum.SUCCESS))
        .isEqualTo(ResponseStatus.SUCCESS);
  }

  @Test
  @DisplayName("Should map FAILURE enum correctly")
  void test_MapStatus_Failure() {
    assertThat(mapper.mapStatus(ResponseStatus.FAILURE)).isEqualTo(
        com.mycompany.product.catalog.model.infra.jpa.postgresql.AuditLog.ResponseStatusEnum.FAILURE);
    assertThat(mapper.mapStatus(
        com.mycompany.product.catalog.model.infra.jpa.postgresql.AuditLog.ResponseStatusEnum.FAILURE))
        .isEqualTo(ResponseStatus.FAILURE);
  }

  @Test
  @DisplayName("Should map null enum to null")
  void test_MapStatus_Null() {
    assertThat(mapper.mapStatus((ResponseStatus) null)).isNull();
    assertThat(mapper.mapStatus(
        (com.mycompany.product.catalog.model.infra.jpa.postgresql.AuditLog.ResponseStatusEnum) null)).isNull();
  }
}
