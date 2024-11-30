package com.gogym.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
//
@Getter
@AllArgsConstructor
public enum SuccessCode implements ResponseCode {
    SUCCESS(HttpStatus.OK, "200", "요청이 성공적으로 실행되었습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
