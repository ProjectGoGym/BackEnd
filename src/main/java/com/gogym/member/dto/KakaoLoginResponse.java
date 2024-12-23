package com.gogym.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class KakaoLoginResponse {
  private final boolean isExistingUser;
  private final String token;
}

