package com.mycompany.catalog.mcp.model.domain;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Domain AuditLog.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AuditLog {

  private Long id;

  private String userId;

  private String toolName;

  private String parameters;

  private Instant timestamp;

  private ResponseStatus responseStatus;

  /**
   * Response status for audit log entries.
   */
  public enum ResponseStatus {
    SUCCESS, FAILURE
  }

}
