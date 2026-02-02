package com.mycompany.exception;

import java.io.IOException;
import org.apache.camel.ValidationException;

/**
 * Resolves exception types to ErrorMeta objects using Java 17 pattern matching.
 *
 * <p>IMPORTANT: All error messages returned to clients are sanitized and generic
 * to prevent exposing internal system details, sensitive data, or security information.
 * Actual exception details are logged server-side for debugging purposes.
 */
public class ErrorMetaResolver {

  /**
   * Resolves an exception to its corresponding ErrorMeta.
   *
   * @param cause the exception to resolve
   * @return the corresponding ErrorMeta, or a default error if no mapping found
   */
  public static ErrorMeta resolve(Throwable cause) {
    if (cause == null) {
      return ErrorMeta.defaultError();
    }

    // Validation Errors (400)
    if (cause instanceof ValidationException) {
      return ErrorMeta.of("ERROR-VALIDATION-001", "Request validation failed", ErrorMeta.BAD_REQUEST);
    }

    // Not Found Errors (404)
    if (cause instanceof DataNotFoundException) {
      return ErrorMeta.of("ERROR-NOT-FOUND-001", "Requested resource not found", ErrorMeta.NOT_FOUND);
    }

    // IO Errors (500)
    if (cause instanceof IOException) {
      return ErrorMeta.of("ERROR-IO-001", "Error processing request", ErrorMeta.INTERNAL_SERVER_ERROR);
    }

    // Default for unknown errors
    return ErrorMeta.defaultError();
  }

}
