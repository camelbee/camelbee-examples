package com.mycompany.catalog.mcp.model.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Domain Error.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Error {

  private String code;

  private String message;

}
