package com.gogym.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

  // 400 BAD REQUEST
  REQUEST_VALIDATION_FAIL(BAD_REQUEST, "잘못된 요청 값입니다."),

  // 401 UNAUTHORIZED
  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증에 실패했습니다."),
  INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
  EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
  EMAIL_NOT_VERIFIED(NOT_FOUND, "이메일 인증이 완료되지 않았습니다."),

  // 403 FORBIDDEN
  FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다."),

  // 404 NOT FOUND
  EMAIL_NOT_FOUND(NOT_FOUND, "이메일을 찾을 수 없습니다."),
  MEMBER_NOT_FOUND(NOT_FOUND, "회원을 찾을 수 없습니다."),
  NOTIFICATION_NOT_FOUND(NOT_FOUND, "알림을 찾을 수 없습니다."),
  CHATROOM_NOT_FOUND(NOT_FOUND, "채팅방을 찾을 수 없습니다."),
  CHAT_MESSAGE_NOT_FOUND(NOT_FOUND, "채팅 메시지를 찾을 수 없습니다."),
  CITY_NOT_FOUND(NOT_FOUND, "도시를 찾을 수 없습니다."),
  GYM_PAY_NOT_FOUND(NOT_FOUND, "짐페이를 찾을 수 없습니다. 짐페이를 개설해주세요."),
  PAYMENT_NOT_FOUND(NOT_FOUND, "결제 정보를 찾을 수 없습니다."),

  // 409 CONFLICT
  DUPLICATE_EMAIL(CONFLICT, "이미 사용 중인 이메일입니다."),
  DUPLICATE_NICKNAME(CONFLICT, "이미 사용 중인 닉네임입니다."),
  ALREADY_READ(CONFLICT, "이미 확인한 알림입니다."),
  CHATROOM_ALREADY_EXISTS(CONFLICT, "이미 존재하는 채팅방입니다."),

  // 500 INTERNAL SERVER ERROR
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");

  private final HttpStatus httpStatus;
  private final String message;
}

