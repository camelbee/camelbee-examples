package com.mycompany.product.catalog.model.infra.jpa.postgresql;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA Entity for audit logging MCP tool calls.
 */
@Entity
@Table(name = "CAMELBEE_AUDIT_LOG_TABLE")
@NoArgsConstructor
@Getter
@Setter
public class AuditLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id")
  private String userId;

  @Column(name = "tool_name")
  private String toolName;

  @Column(name = "parameters", columnDefinition = "TEXT")
  private String parameters;

  @Column(name = "timestamp_utc")
  private Instant timestampUtc;

  @Column(name = "response_status")
  @Enumerated(EnumType.STRING)
  private ResponseStatusEnum responseStatus;

  public AuditLog(String userId, String toolName, String parameters, Instant timestampUtc, ResponseStatusEnum responseStatus) {
    this.userId = userId;
    this.toolName = toolName;
    this.parameters = parameters;
    this.timestampUtc = timestampUtc;
    this.responseStatus = responseStatus;
  }

  /**
   * Response status enum for audit log.
   */
  public enum ResponseStatusEnum {
    SUCCESS, FAILURE
  }

}
