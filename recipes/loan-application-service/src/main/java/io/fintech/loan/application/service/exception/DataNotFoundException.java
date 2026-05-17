package io.fintech.loan.application.service.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Custom exception to indicate data not found.
 */
@Getter
@ToString(callSuper = true)
@NoArgsConstructor
public class DataNotFoundException extends RuntimeException {

  private static final String DEFAULT_CODE = "ERROR-NOT-FOUND-001";
  private static final String DEFAULT_MESSAGE = "Resource not found";

  private String errorCode = DEFAULT_CODE;

  public DataNotFoundException(String errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }

  public DataNotFoundException(String errorCode, String message, Throwable cause) {
    super(message, cause);
    this.errorCode = errorCode;
  }

  public DataNotFoundException(String message) {
    super(message);
    this.errorCode = DEFAULT_CODE;
  }

  public DataNotFoundException(Throwable cause) {
    super(DEFAULT_MESSAGE, cause);
    this.errorCode = DEFAULT_CODE;
  }
}