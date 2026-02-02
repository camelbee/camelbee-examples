package com.mycompany.utils;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.camel.Exchange;

/**
 * Utility class for handling Apache Camel Exchange-related operations.
 * This class provides static utility methods for extracting information from file names
 * used in Camel exchanges.
 *
 * <p>The class is designed to be stateless and thread-safe. All methods are static
 * and the class cannot be instantiated.
 *
 * @since 1.0
 * @author camelbee
 */
public final class ExchangeHelper {

  private static final String FILENAME_PATTERN = ".*?-([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})\\.(.*)";
  private static final Pattern COMPILED_PATTERN = Pattern.compile(FILENAME_PATTERN);

  private ExchangeHelper() {
    // Private constructor to prevent instantiation
  }

  /**
   * Extracts UUID transaction ID from a filename.
   * Example: for "createorder-success-request-779d2950-7f02-440a-a4a3-b36b82ec6c29.json"
   * returns "779d2950-7f02-440a-a4a3-b36b82ec6c29"
   *
   * @param fileName the name of the file to parse
   * @return the extracted UUID transaction ID
   * @throws IllegalArgumentException if the filename is null or doesn't match the expected pattern
   */
  public static String extractTransactionId(String fileName) {
    if (fileName == null) {
      throw new IllegalArgumentException("Filename cannot be null");
    }

    Matcher matcher = COMPILED_PATTERN.matcher(fileName);

    if (matcher.matches() && matcher.groupCount() >= 1) {
      return matcher.group(1); // Group 1 contains the UUID
    }

    throw new IllegalArgumentException(
        String.format("Invalid filename format: %s. Expected format: <any-name>-<UUID>.<extension>",
            fileName)
    );
  }

  /**
   * Converts the "transactionId" header from byte[] to String in-place in the exchange.
   * If the header is not present or not a byte array, nothing happens.
   *
   * @param exchange the Camel Exchange object
   */
  public static void normalizeTransactionIdHeader(Exchange exchange) {
    if (exchange == null) {
      throw new IllegalArgumentException("Exchange cannot be null");
    }

    byte[] headerBytes = exchange.getIn().getHeader("transactionId", byte[].class);
    if (headerBytes != null) {
      String transactionId = new String(headerBytes, StandardCharsets.UTF_8);
      exchange.getIn().setHeader("transactionId", transactionId);
    }
  }
}