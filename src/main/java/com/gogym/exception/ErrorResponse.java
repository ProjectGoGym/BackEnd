package com.gogym.exception;

import com.gogym.common.response.ErrorCode;

public record ErrorResponse(
    Integer status,
    String name,
    String message
) {
  public static ErrorResponse from(ErrorCode errorCode) {
    return new ErrorResponse(errorCode.getHttpStatus().value(), errorCode.name(), errorCode.getMessage());
  }
}
