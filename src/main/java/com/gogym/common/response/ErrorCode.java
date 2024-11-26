package com.gogym.common.response;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
  // 400 Bad Request
  REQUEST_VALIDATION_FAIL(BAD_REQUEST, "400", "잘못된 요청 값입니다."),
  DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "400", "이미 존재하는 이메일입니다."),
  DUPLICATE_NICKNAME(HttpStatus.BAD_REQUEST, "400", "이미 존재하는 닉네임입니다."),
  EMAIL_NOT_FOUND(HttpStatus.BAD_REQUEST, "400", "존재하지 않는 이메일입니다."),
  INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "400", "비밀번호가 일치하지 않습니다."),

  // 401 Unauthorized

  // 403 Forbidden

  // 404 Not Found

  // 409 Conflict

  // 500
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "500", "서버 내부 오류입니다.");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;
}
