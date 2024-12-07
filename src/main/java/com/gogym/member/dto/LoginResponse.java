package com.gogym.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {
  // 프론트앤드 분들 요청사항으로 로그인 후 반환값을 추가했습니다.
  private String email;
  private String name;
  private String nickname;
  private String phone;
}

