package com.gogym.member.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignUpResponse {
  private Long userId; //유저id
  private String nickname; //닉네임
  private String email; //이메일
}
