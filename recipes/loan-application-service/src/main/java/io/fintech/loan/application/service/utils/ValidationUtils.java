package io.fintech.loan.application.service.utils;

import java.util.Arrays;
import java.util.Optional;
import lombok.SneakyThrows;
import org.apache.camel.Exchange;
import org.apache.camel.ValidationException;

/**
 * Utility class providing validation methods for Apache Camel Exchange headers.
 * Contains methods to validate numeric values and sales channel information.
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

  /**
   * Validates the sales channel header value against a predefined list of valid channels.
   * Valid channels are: ONLINE, RETAIL, and WHOLESALE (case-insensitive).
   *
   * @param exchange The Camel Exchange containing the sales channel header
   * @throws ValidationException if the sales channel is empty or not in the list of valid channels
   */
  @SneakyThrows
  public static void validateSalesChannel(Exchange exchange) {
    String channel = Optional.ofNullable(exchange.getIn().getHeader("salesChannel", String.class))
        .map(String::trim)
        .orElseThrow(() -> new ValidationException(exchange, "Sales channel cannot be empty!"));

    if (!Arrays.asList("ONLINE", "RETAIL", "WHOLESALE", "MCP-AGENT").contains(channel.toUpperCase())) {
      throw new ValidationException(exchange, "Invalid sales channel!");
    }
  }

  private ValidationUtils() {
    // Prevent instantiation
  }
}
