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
  private String name; //이름

  @Email
  @NotBlank
  private String email; //이메일

  @NotBlank
  private String nickname; //닉네임

  @NotBlank
  private String phone; //핸드폰

  @NotBlank
  private String password; //비밀번호

  @NotBlank
  @Pattern(regexp = "^(USER|ADMIN)$")
  private String role; //역할

  private String profileImageUrl; //프로필 이미지
  private String interestArea1; //관심지역 1 and 2
  private String interestArea2;
}
