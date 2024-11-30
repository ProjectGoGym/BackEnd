package com.gogym.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ApplicationResponse<T> {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private final T data;
  private final String responseCode;
  private final String responseMessage;


  public static <T> ApplicationResponse<T> ok(T data, String code, String message) {
    return new ApplicationResponse<>(data, code, message);
    }

  public static ApplicationResponse<Void> noData(String code, String message) {
    return new ApplicationResponse<>(null, code, message);
    }

  public static ApplicationResponse<Void> error(String code, String message) {
    return new ApplicationResponse<>(null, code, message);
    }
}
