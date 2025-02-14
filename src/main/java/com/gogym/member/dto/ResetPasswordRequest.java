package com.gogym.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResetPasswordRequest {

  @Email
  @NotBlank
  private String email;

  @NotBlank
  private String currentPassword;

  @NotBlank
  private String newPassword;
}
