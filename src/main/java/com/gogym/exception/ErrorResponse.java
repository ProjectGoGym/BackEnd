package com.gogym.exception;

public record ErrorResponse(
    String name,
    String message
) {
  public static ErrorResponse from(ErrorCode errorCode) {
    return new ErrorResponse(errorCode.name(), errorCode.getMessage());
  }

  public static ErrorResponse withMessage(ErrorCode errorCode, String message) {
    return new ErrorResponse(errorCode.name(), message);
  }
}
