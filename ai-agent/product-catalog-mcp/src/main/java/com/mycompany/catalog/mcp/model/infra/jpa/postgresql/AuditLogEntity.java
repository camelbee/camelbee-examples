package com.mycompany.catalog.mcp.model.infra.jpa.postgresql;

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
 * AuditLog JPA Entity for tracking MCP tool calls.
 */
@Entity
@Table(name = "CAMELBEE_AUDIT_LOG")
@NoArgsConstructor
@Getter
@Setter
public class AuditLogEntity {

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
  private Instant timestamp;

  @Enumerated(EnumType.STRING)
  @Column(name = "response_status")
  private ResponseStatus responseStatus;

  public AuditLogEntity(String userId, String toolName, String parameters, Instant timestamp, ResponseStatus responseStatus) {
    this.userId = userId;
    this.toolName = toolName;
    this.parameters = parameters;
    this.timestamp = timestamp;
    this.responseStatus = responseStatus;
  }

  /**
   * Response status enum.
   */
  public enum ResponseStatus {
    SUCCESS, FAILURE
  }

}
