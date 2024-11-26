package com.gogym.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

public class MemberDto {

  @Getter
  @Setter
  public static class SignUpRequest {
    @NotBlank
    private String name;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String nickname;

    @NotBlank
    private String phone;

    @NotBlank
    private String password;
    
    @NotBlank
    @Pattern(regexp = "^(USER|ADMIN)$")
    private String role;

    private String profileImageUrl;
    private String interestArea1;
    private String interestArea2;
  }

  @Data
  public static class LoginRequest {
    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class LoginResponse {
    private String nickname;
    private String token;
  }

  @Data
  public static class ResetPasswordRequest {
    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String newPassword;
  }

  @Data
  public static class VerifyCodeRequest {
    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String code;
  }
}


