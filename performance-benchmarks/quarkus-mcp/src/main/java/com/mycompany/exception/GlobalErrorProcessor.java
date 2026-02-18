package com.mycompany.exception;

import com.mycompany.exception.response.ResponseFormatter;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelExchangeException;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

/**
 * Global Error Processor for handling exceptions in Apache Camel routes.
 * Orchestrates protocol-specific error mappers to convert exceptions to appropriate formats.
 *
 * <p>This processor:
 * 1. Extracts the root cause of exceptions
 * 2. Resolves exception to ErrorMeta using ErrorMetaResolver
 * 3. Delegates to appropriate protocol-specific mapper
 *
 * @see ResponseFormatter
 * @see ErrorMetaResolver
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class GlobalErrorProcessor implements Processor {

  private static final String ERROR_MESSAGE_FORMAT = "{} {}";
  private static final int MAX_EXCEPTION_DEPTH = 10;

  /**
   * Instance of all available error mappers, injected by CDI.
   * Mappers are automatically discovered as CDI beans and accessed via Instance<T>.
   */
  private final Instance<ResponseFormatter> mappers;
  /**
   * Sorted list of mappers by priority.
   */
  private List<ResponseFormatter> sortedMappers;

  /**
   * Initializes the GlobalErrorProcessor by sorting response formatters by priority.
   */
  @PostConstruct
  public void init() {
    // Sort mappers by priority on startup
    sortedMappers = mappers.stream()
        .sorted(Comparator.comparingInt(ResponseFormatter::getPriority))
        .toList();

    log.info("Initialized GlobalErrorProcessor with {} mappers: {}",
        sortedMappers.size(),
        sortedMappers.stream()
            .map(m -> m.getClass().getSimpleName())
            .toList());
  }

  @Override
  public void process(Exchange exchange) {
    Throwable cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);

    if (cause == null) {
      log.warn("No exception found in exchange");
      return;
    }

    // Recursively retrieve the root cause from CamelExchangeException
    cause = extractRootCause(cause);

    // Handle IOException as a special case to identify the underlying cause
    if (cause.getClass() == IOException.class) {
      cause = extractIoExceptionCause(cause);
    }

    // Resolve exception to ErrorMeta
    ErrorMeta meta = ErrorMetaResolver.resolve(cause);

    // Log based on error severity
    logError(meta, cause);

    // Find appropriate mapper and handle the error
    boolean handled = false;
    for (ResponseFormatter mapper : sortedMappers) {
      if (mapper.supports(exchange)) {
        try {
          log.trace("Using {} to handle error for endpoint {}",
              mapper.getClass().getSimpleName(),
              exchange.getFromEndpoint());

          mapper.format(exchange, meta);
          handled = true;

        } catch (Exception e) {
          log.error("Error in mapper {}: {}",
              mapper.getClass().getSimpleName(),
              e.getMessage(), e);
        }
        break;
      }
    }

    if (!handled) {
      log.error("No matching mapper found for exchange from endpoint: {}. Error: {} - {}",
          exchange.getFromEndpoint(),
          meta.code(),
          meta.message());

      // Set a basic error response
      setDefaultErrorResponse(exchange, meta);
    }
  }

  /**
   * Extracts the root cause of an exception, unwrapping CamelExchangeException.
   *
   * @param cause the exception to unwrap
   * @return the root cause
   */
  private Throwable extractRootCause(Throwable cause) {
    int depth = 0;
    while (cause instanceof CamelExchangeException ce && ce.getCause() != null && depth < MAX_EXCEPTION_DEPTH) {
      cause = ce.getCause();
      depth++;
    }
    return cause;
  }

  /**
   * Extracts the root cause of an exception, unwrapping IOException.
   *
   * @param ioEx the exception to unwrap
   * @return the root cause
   */
  private static Throwable extractIoExceptionCause(Throwable ioEx) {

    if (ioEx != null && ioEx.getCause() != null) {
      return ioEx.getCause();
    }

    return ioEx;
  }

  /**
   * Logs the error based on its severity.
   */
  private void logError(ErrorMeta meta, Throwable cause) {
    if (meta.status() >= 500) {
      log.error(ERROR_MESSAGE_FORMAT, "Internal Error:", cause.getLocalizedMessage(), cause);
    } else if (meta.status() >= 400) {
      log.warn(ERROR_MESSAGE_FORMAT, "Client Error:", cause.getLocalizedMessage());
    } else {
      log.debug(ERROR_MESSAGE_FORMAT, "Unknown Error:", cause.getLocalizedMessage());
    }
  }

  /**
   * Sets a default error response when no mapper can handle the exchange.
   */
  private void setDefaultErrorResponse(Exchange exchange, ErrorMeta meta) {
    exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, meta.status());
    exchange.getMessage().setBody(String.format(
        "{\"code\":\"%s\",\"message\":\"%s\"}",
        meta.code(),
        meta.message()
    ));
  }
}
