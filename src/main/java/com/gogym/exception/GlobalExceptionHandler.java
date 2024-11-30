package com.gogym.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  // 사용자 정의 예외 처리
  @ExceptionHandler(CustomException.class)
  public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
    ErrorCode errorCode = e.getErrorCode();
    ErrorResponse response = ErrorResponse.from(errorCode);

    log.error("CustomException: {}, Code: {}", errorCode.getMessage(), errorCode.getCode());

    return ResponseEntity.ok(response);
  }

  // 예상치 못한 런타임 예외 처리
  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ErrorResponse> handleUnexpectedException(RuntimeException e) {

    ErrorResponse response = ErrorResponse.from(ErrorCode.INTERNAL_SERVER_ERROR);

    log.error("Unexpected Exception: {}", e.getMessage(), e);

    return ResponseEntity.ok(response);
  }

  // 요청 검증 실패 처리
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
      HttpServletRequest request, MethodArgumentNotValidException e) {
    String errorMessage = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
    String requestURI = request.getRequestURI();

    ErrorResponse response = ErrorResponse.from(ErrorCode.REQUEST_VALIDATION_FAIL);

    log.error("MethodArgumentNotValidException: {}, Request URI: {}", errorMessage, requestURI);

    return ResponseEntity.ok(response);
  }

  // 제약 조건 위반 예외 처리
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraintViolationException(HttpServletRequest request, ConstraintViolationException e) {
    String errorMessage = e.getMessage();
    String requestURI = request.getRequestURI();

    ErrorResponse response = ErrorResponse.from(ErrorCode.REQUEST_VALIDATION_FAIL);

    log.error("ConstraintViolationException: {}, Request URI: {}", errorMessage, requestURI);

    return ResponseEntity.ok(response);
  }
}

