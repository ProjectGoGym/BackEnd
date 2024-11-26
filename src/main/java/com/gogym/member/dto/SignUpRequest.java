package com.gogym.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignUpRequest {

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
