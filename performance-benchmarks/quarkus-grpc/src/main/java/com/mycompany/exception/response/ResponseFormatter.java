package com.mycompany.exception.response;

import com.mycompany.exception.ErrorMeta;
import org.apache.camel.Exchange;

/**
 * Interface for protocol-specific error mappers.
 * Each implementation handles error mapping for a specific protocol (REST, SOAP, gRPC, etc.)
 */
public interface ResponseFormatter {

  /**
   * Determines if this mapper can handle the given exchange based on endpoint type.
   *
   * @param exchange the Camel exchange
   * @return true if this mapper supports the exchange's protocol
   */
  boolean supports(Exchange exchange);

  /**
   * Handles the error for the given exchange, converting it to the appropriate format.
   *
   * @param exchange the Camel exchange
   * @param meta     the resolved error metadata
   */
  void format(Exchange exchange, ErrorMeta meta);

  /**
   * Priority order for mapper selection (lower values = higher priority).
   * Default is 100. Override to change mapper selection order.
   *
   * @return the priority value
   */
  default int getPriority() {
    return 100;
  }
}
