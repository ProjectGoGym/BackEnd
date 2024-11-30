package com.gogym.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

  // 400 Bad Request : 요청이 잘못된 경우
  REQUEST_VALIDATION_FAIL(HttpStatus.BAD_REQUEST, "400", "잘못된 요청 값입니다."),
  INVALID_EMAIL_FORMAT(HttpStatus.BAD_REQUEST, "400", "이메일 형식이 올바르지 않습니다."),
  INVALID_TOKEN(HttpStatus.BAD_REQUEST, "400", "토큰이 잘못되었습니다."),
  PASSWORDS_DO_NOT_MATCH(HttpStatus.BAD_REQUEST, "400", "새 비밀번호가 일치하지 않습니다."),
  EXPIRED_TOKEN(HttpStatus.BAD_REQUEST, "400", "토큰이 만료되었습니다."),
  
  // 401 Unauthorized : 인증 실패 관련
  EMAIL_NOT_FOUND(HttpStatus.UNAUTHORIZED, "401", "존재하지 않는 이메일입니다."),
  INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "401", "비밀번호가 일치하지 않습니다."),
  MEMBER_NOT_FOUND(HttpStatus.UNAUTHORIZED, "401", "존재하지 않는 회원 입니다."),
  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."),
  
  // 403 Forbidden : 권한 부족
  FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "해당 채팅방에 참여 중이 아닙니다."),
  // 404 not found
  REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "404", "찾을 수 없습니다."),
  CHATROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "CHATROOM_NOT_FOUND", "채팅방을 찾을 수 없습니다."),
  
  // 409 Conflict : 충돌 관련
  DUPLICATE_EMAIL(HttpStatus.CONFLICT, "409", "이미 존재하는 이메일입니다."),
  DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "409", "이미 존재하는 닉네임입니다."),
  ALREADY_READ(HttpStatus.CONFLICT, "409", "이미 확인한 알림입니다."),
  CHATROOM_ALREADY_EXISTS(HttpStatus.CONFLICT, "CHATROOM_ALREADY_EXISTS", "이미 존재하는 채팅방입니다."),
  
  // 500 Internal Server Error: 서버 내부 문제
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "500", "서버 내부 오류입니다.");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;
}

