package com.gogym.exception;

public record ErrorResponse(
    String name,
    String message
) {
  public static ErrorResponse from(ErrorCode errorCode) {
    return new ErrorResponse(errorCode.name(), errorCode.getMessage());
  }
}


