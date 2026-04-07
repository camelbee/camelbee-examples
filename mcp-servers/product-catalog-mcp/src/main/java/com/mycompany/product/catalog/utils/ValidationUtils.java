package com.mycompany.product.catalog.utils;

import lombok.SneakyThrows;
import org.apache.camel.Exchange;
import org.apache.camel.ValidationException;

/**
 * Utility class providing validation methods for Apache Camel Exchange headers.
 * Contains methods to validate numeric values.
 *
 * <p>All path parameters, headers, and query parameters must be validated using appropriate methods
 * from this class to prevent injection attacks (SQL injection, XML injection, script injection, etc.).
 */
public class ValidationUtils {

  /**
   * Validates that a numeric header value falls within a specified range.
   *
   * @param exchange   The Camel Exchange containing the header
   * @param headerName The name of the header to validate
   * @param min        The minimum acceptable value (inclusive)
   * @param max        The maximum acceptable value (inclusive)
   * @throws ValidationException if the header value is not a valid integer or falls outside the specified range
   */
  @SneakyThrows
  public static void validateNumericHeader(Exchange exchange, String headerName, int min, int max) {
    try {
      int value = Integer.parseInt(exchange.getIn().getHeader(headerName, String.class));
      if (value < min || value > max) {
        throw new ValidationException(exchange,
            String.format("%s must be between %d and %d!", headerName, min, max));
      }
    } catch (NumberFormatException e) {
      throw new ValidationException(exchange, headerName + " must be a valid integer!");
    }
  }

  private ValidationUtils() {
    // Prevent instantiation
  }
}
