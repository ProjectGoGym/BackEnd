package com.gogym.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPasswordRequest {

  @Email
  @NotBlank
  private String email; //이메일

  @NotBlank
  private String newPassword; //새 비밀번호

  @NotBlank
  private String confirmPassword; //새 비밀번호 확인
}
