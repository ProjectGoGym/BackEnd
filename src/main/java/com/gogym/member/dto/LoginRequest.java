package com.gogym.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

  @Email
  @NotBlank
  private String email; //이메일

  @NotBlank
  private String password; //패스워드
}
