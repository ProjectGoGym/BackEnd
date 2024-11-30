package com.gogym.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
  //공통 에러
  INVALID_REQUEST("400", "잘못된 요청입니다."),
  REQUEST_VALIDATION_FAIL("400", "요청 검증에 실패했습니다."),
  INVALID_PASSWORD("400", "비밀번호가 올바르지 않습니다."),

  //401
  UNAUTHORIZED("401", "인증이 필요합니다."),
  INVALID_TOKEN("401", "유효하지 않은 토큰입니다."),
  EXPIRED_TOKEN("401", "만료된 토큰입니다."),

  //403
  FORBIDDEN("403", "권한이 없습니다."),
  
  //404
  NOT_FOUND("404", "요청하신 리소스를 찾을 수 없습니다."),
  EMAIL_NOT_FOUND("404", "이메일을 찾을 수 없습니다."),
  MEMBER_NOT_FOUND("404", "회원을 찾을 수 없습니다."),
  EMAIL_NOT_VERIFIED("404", "이메일 인증이 완료되지 않았습니다."),

  //409
  DUPLICATE_EMAIL("409", "이미 사용 중인 이메일입니다."),
  DUPLICATE_NICKNAME("409", "이미 사용 중인 닉네임입니다."),

  //500
  INTERNAL_SERVER_ERROR("500", "서버 오류가 발생했습니다.");

  private final String code;
  private final String message;
}

