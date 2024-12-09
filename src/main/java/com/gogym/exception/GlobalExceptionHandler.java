package com.gogym.exception;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(CustomException.class)
  public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
    ErrorCode errorCode = e.getErrorCode();
    ErrorResponse response = ErrorResponse.from(errorCode);

    log.error("CustomException: {}, Code: {}", errorCode.getMessage(), errorCode.getHttpStatus());

    return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ErrorResponse> handleUnexpectedException(RuntimeException e) {

    ErrorResponse response = ErrorResponse.from(ErrorCode.INTERNAL_SERVER_ERROR);

    log.error("Unexpected Exception: {}", e.getMessage(), e);

    return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus()).body(response);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
      HttpServletRequest request, MethodArgumentNotValidException e) {
    String errorMessage = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
    String requestURI = request.getRequestURI();

    ErrorResponse response = ErrorResponse.from(ErrorCode.REQUEST_VALIDATION_FAIL);

    log.error("MethodArgumentNotValidException: {}, Request URI: {}", errorMessage, requestURI);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraintViolationException(
      HttpServletRequest request, ConstraintViolationException e) {
    String errorMessage = e.getMessage();
    String requestURI = request.getRequestURI();

    ErrorResponse response = ErrorResponse.from(ErrorCode.REQUEST_VALIDATION_FAIL);

    log.error("ConstraintViolationException: {}, Request URI: {}", errorMessage, requestURI);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(WebClientResponseException.class)
  public ResponseEntity<ErrorResponse> handleWebClientResponseException(
      HttpServletRequest request, WebClientResponseException e) {
    String errorMessage = e.getResponseBodyAsString();
    String requestURI = request.getRequestURI();

    ErrorResponse response = ErrorResponse.withMessage(ErrorCode.PORTONE_API_CALL_FAILED, errorMessage);

    log.error("WebClientResponseException: {}, Status: {}, Request URI: {}", errorMessage,
        e.getStatusCode(), requestURI);

    return ResponseEntity.status(e.getStatusCode()).body(response);
  }
}

