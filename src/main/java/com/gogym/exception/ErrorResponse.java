package com.gogym.exception;

public record ErrorResponse(
    String code,
    String name,
    String message
) {
  public static ErrorResponse from(ErrorCode errorCode) {
    return new ErrorResponse(errorCode.getCode(), errorCode.name(), errorCode.getMessage());
  }
}


