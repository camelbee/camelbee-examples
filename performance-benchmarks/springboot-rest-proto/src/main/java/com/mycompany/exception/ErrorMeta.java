package com.mycompany.exception;

/**
 * Represents error metadata with code, message, and status.
 * This is the common error representation used as an intermediate structure
 * before converting to protocol-specific error responses.
 */
public record ErrorMeta(String code, String message, int status) {

  public static final String DEFAULT_CODE = "ERROR-UNKNOWN-001";
  public static final String DEFAULT_MESSAGE = "An unexpected error occurred";
  public static final int UNKNOWN_ERROR = 100;

  public static final int BAD_REQUEST = 400;
  public static final int UNAUTHORIZED = 401;
  public static final int FORBIDDEN = 403;
  public static final int NOT_FOUND = 404;
  public static final int INTERNAL_SERVER_ERROR = 500;

  /**
   * Creates a default error meta for unexpected errors.
   */
  public static ErrorMeta defaultError() {
    return new ErrorMeta(DEFAULT_CODE, DEFAULT_MESSAGE, INTERNAL_SERVER_ERROR);
  }

  /**
   * Creates an error meta with custom code, message, and status.
   */
  public static ErrorMeta of(String code, String message, int status) {
    return new ErrorMeta(code, message, status);
  }
}