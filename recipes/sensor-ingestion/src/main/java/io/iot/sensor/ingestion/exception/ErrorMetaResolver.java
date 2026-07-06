package io.iot.sensor.ingestion.exception;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
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

    if (cause instanceof ValueInstantiationException) {
      return ErrorMeta.of("ERROR-VALIDATION-002", "Invalid input data format", ErrorMeta.BAD_REQUEST);
    }

    // JSON Parsing Errors (400)
    if (cause instanceof JsonParseException) {
      return ErrorMeta.of("ERROR-JSON-001", "Invalid JSON format in request", ErrorMeta.BAD_REQUEST);
    }

    if (cause instanceof InvalidFormatException) {
      return ErrorMeta.of("ERROR-JSON-002", "Invalid data format in request", ErrorMeta.BAD_REQUEST);
    }

    if (cause instanceof MismatchedInputException) {
      return ErrorMeta.of("ERROR-JSON-003", "Request data does not match expected format", ErrorMeta.BAD_REQUEST);
    }

    if (cause instanceof JsonMappingException) {
      return ErrorMeta.of("ERROR-JSON-004", "Invalid data format in request\"", ErrorMeta.BAD_REQUEST);
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
