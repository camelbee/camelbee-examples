package com.mycompany.product.catalog.model.domain;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Domain AuditLog.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class AuditLog {

  private Long id;

  private String userId;

  private String toolName;

  private String parameters;

  private Instant timestamp;

  private ResponseStatus responseStatus;

}
