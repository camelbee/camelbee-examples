package com.mycompany.exception.response;

import com.mycompany.exception.ErrorMeta;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;

/**
 * Error mapper for gRPC endpoints.
 * Maps errors to gRPC Status codes.
 */
@ApplicationScoped
@Slf4j
public class GrpcErrorResponseFormatter implements ResponseFormatter {

  @Override
  public boolean supports(Exchange exchange) {
    String from = exchange.getFromEndpoint().toString();
    return from.contains("grpc");
  }

  @Override
  public void format(Exchange exchange, ErrorMeta meta) {
    log.debug("Handling gRPC error - Code: {}, Message: {}, HTTP Status: {}",
        meta.code(), meta.message(), meta.status());

    // Map to gRPC status
    Status status = mapToGrpcStatus(meta.code(), meta.message(), meta.status());

    // Create the gRPC exception
    StatusRuntimeException grpcException = status.asRuntimeException();

    // Don't handle the exception - let it propagate to gRPC framework
    exchange.setProperty(Exchange.ERRORHANDLER_HANDLED, false);

    // Set the exception so gRPC can send it to the client
    exchange.setException(grpcException);

    // Clear the body - gRPC will send the status instead
    exchange.getIn().setBody(null);
  }

  /**
   * Maps HTTP status codes and error information to gRPC Status.
   * This is the reverse mapping of what RestErrorMapper does for gRPC errors.
   */
  private Status mapToGrpcStatus(String errorCode, String errorMessage, int status) {
    String description = String.format("%s - %s", errorCode, errorMessage);

    // Map based on HTTP status code
    switch (status) {
      case 400:
        // Bad Request -> INVALID_ARGUMENT
        if (errorCode.contains("VALIDATION") || errorMessage.toLowerCase().contains("validation")) {
          return Status.INVALID_ARGUMENT.withDescription(description);
        }
        if (errorMessage.toLowerCase().contains("out of range")) {
          return Status.OUT_OF_RANGE.withDescription(description);
        }
        return Status.INVALID_ARGUMENT.withDescription(description);

      case 401:
        // Unauthorized -> UNAUTHENTICATED
        return Status.UNAUTHENTICATED.withDescription(description);

      case 403:
        // Forbidden -> PERMISSION_DENIED
        return Status.PERMISSION_DENIED.withDescription(description);

      case 404:
        // Not Found -> NOT_FOUND
        return Status.NOT_FOUND.withDescription(description);

      case 408:
        // Request Timeout -> DEADLINE_EXCEEDED
        return Status.DEADLINE_EXCEEDED.withDescription(description);

      case 409:
        // Conflict -> ALREADY_EXISTS or ABORTED
        if (errorMessage.toLowerCase().contains("already exists")
            || errorMessage.toLowerCase().contains("duplicate")) {
          return Status.ALREADY_EXISTS.withDescription(description);
        }
        return Status.ABORTED.withDescription(description);

      case 412:
        // Precondition Failed -> FAILED_PRECONDITION
        return Status.FAILED_PRECONDITION.withDescription(description);

      case 429:
        // Too Many Requests -> RESOURCE_EXHAUSTED
        return Status.RESOURCE_EXHAUSTED.withDescription(description);

      case 499:
        // Client Closed Request -> CANCELLED
        return Status.CANCELLED.withDescription(description);

      case 501:
        // Not Implemented -> UNIMPLEMENTED
        return Status.UNIMPLEMENTED.withDescription(description);

      case 503:
        // Service Unavailable -> UNAVAILABLE
        return Status.UNAVAILABLE.withDescription(description);

      case 504:
        // Gateway Timeout -> DEADLINE_EXCEEDED
        return Status.DEADLINE_EXCEEDED.withDescription(description);

      default:
        // For 5xx errors, check if it's a data loss scenario
        if (errorCode.contains("DATA") && errorCode.contains("LOSS")) {
          return Status.DATA_LOSS.withDescription(description);
        }
        // Default to INTERNAL for other server errors
        if (status >= 500) {
          return Status.INTERNAL.withDescription(description);
        }
        // For other 4xx errors, default to INVALID_ARGUMENT
        if (status >= 400 && status < 500) {
          return Status.INVALID_ARGUMENT.withDescription(description);
        }
        // Unknown status -> UNKNOWN
        return Status.UNKNOWN.withDescription(description);
    }
  }

}
