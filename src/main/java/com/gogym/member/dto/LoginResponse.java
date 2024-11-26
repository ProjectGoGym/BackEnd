package com.gogym.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
  private Long userId; //유저id
  private String nickname; //닉네임    
  private String email; //이메일
  private String token; //토큰
  private String role; //역할, 어드민인지 회원인지
}
