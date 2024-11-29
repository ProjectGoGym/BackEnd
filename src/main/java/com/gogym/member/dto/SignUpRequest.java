package com.gogym.member.dto;

import com.gogym.member.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.gogym.member.entity.Member;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignUpRequest {

  @Email
  @NotBlank
  private String email;

  @NotBlank
  private String password;

  @NotBlank
  private String name;

  @NotBlank
  private String nickname;

  private String phone;

  private String profileImageUrl;

  private String interestArea1;

  private String interestArea2;

  private Role role;
  
  public Member toEntity(String encodedPassword) {
    return Member.builder()
      .name(this.name)
      .email(this.email)
      .nickname(this.nickname)
      .phone(this.phone)
      .password(encodedPassword) // 암호화된 비밀번호 사용
      .role(this.role)
      .profileImageUrl(this.profileImageUrl)
      .interestArea1(this.interestArea1)
      .interestArea2(this.interestArea2)
      .build();
  }
}


