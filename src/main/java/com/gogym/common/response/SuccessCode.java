package com.gogym.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessCode implements ResponseCode {
  
  PASSWORD_RESET_SUCCESS(HttpStatus.OK, "200", "비밀번호가 성공적으로 재설정되었습니다."),
  LOGIN_SUCCESS(HttpStatus.OK, "200", "로그인이 성공적으로 완료되었습니다."),
  SIGN_UP_SUCCESS(HttpStatus.OK, "200", "회원 가입이 완료되었습니다."),
  EMAIL_AVAILABLE(HttpStatus.OK, "200", "사용 가능한 이메일 입니다."),
  LOGOUT_SUCCESS(HttpStatus.OK, "200", "로그아웃 성공"),
  NICKNAME_AVAILABLE(HttpStatus.OK, "200", "사용 가능한 닉네임입니다."),
  EMAIL_VERIFICATION_SUCCESS(HttpStatus.OK, "200", "이메일 인증이 성공적으로 완료되었습니다."),
  
  //data true/false 타입으로 결과를 반환하다보니 error처리를 200 응답 종류로 뒀습니다.
  DUPLICATE_NICKNAME(HttpStatus.OK, "200", "이미 존재하는 닉네임입니다."),
  DUPLICATE_EMAIL(HttpStatus.OK, "200", "이미 존재하는 이메일입니다."),
  
  //일반적인 요청 성공
  SUCCESS(HttpStatus.OK, "200", "요청이 성공적으로 실행되었습니다.");
  
  private final HttpStatus httpStatus;
  private final String code;
  private final String message;
}
