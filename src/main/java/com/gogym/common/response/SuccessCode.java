package com.gogym.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessCode implements ResponseCode {
  
  AUTH_SUCCESS(HttpStatus.OK, "200", "요청이 성공적으로 처리되었습니다."),
  DATA_AVAILABLE(HttpStatus.OK, "200", "요청하신 데이터는 사용 가능합니다."),
  EMAIL_VERIFIED(HttpStatus.OK,"200", "이메일 인증이 완료되었습니다.");
  
  private final HttpStatus httpStatus;
  private final String code;
  private final String message;
}
