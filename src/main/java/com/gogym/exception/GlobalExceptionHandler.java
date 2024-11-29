package com.gogym.exception;

import static com.gogym.common.response.ErrorCode.REQUEST_VALIDATION_FAIL;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import com.gogym.common.response.ErrorCode;
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

  @ExceptionHandler(CustomException.class)
  public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
    ErrorCode errorCode = e.getErrorCode();
    ErrorResponse response = ErrorResponse.from(errorCode);

    log.error("CustomException: {}, HTTP Status: {}", errorCode.getMessage(), errorCode.getHttpStatus());

    return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ErrorResponse> handleUnexpectedException(RuntimeException e) {

    ErrorResponse response = ErrorResponse.from(ErrorCode.INTERNAL_SERVER_ERROR);

    log.error("Unexpected Exception: {}", e.getMessage(), e);

    return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(response);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
      HttpServletRequest request, MethodArgumentNotValidException e) {
    String errorMessage = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
    String requestURI = request.getRequestURI();

    ErrorResponse response = ErrorResponse.from(REQUEST_VALIDATION_FAIL);

    log.error("MethodArgumentNotValidException: {}, Request URI: {}", errorMessage, requestURI);

    return ResponseEntity.status(BAD_REQUEST).body(response);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraintViolationException(HttpServletRequest request, ConstraintViolationException e) {
    String errorMessage = e.getMessage();
    String requestURI = request.getRequestURI();

    ErrorResponse response = ErrorResponse.from(REQUEST_VALIDATION_FAIL);

    log.error("ConstraintViolationException: {}, Request URI: {}", errorMessage, requestURI);

    return ResponseEntity.status(BAD_REQUEST).body(response);
  }
}