package com.gogym.member.dto;

import com.gogym.member.entity.Role;
import com.gogym.member.type.MemberStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.gogym.member.entity.Member;

@Getter
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

  @NotBlank
  private String phone;

  private String profileImageUrl;

  private Long regionId1;

  private Long regionId2;

  private Role role;

  @Setter
  private boolean isKakao;

  public Member toEntity(String encodedPassword) {
    return Member.builder().name(this.name).email(this.email).nickname(this.nickname)
        .phone(this.phone).password(encodedPassword).role(Role.USER).isKakao(this.isKakao)
        .profileImageUrl(this.profileImageUrl).regionId1(this.regionId1).regionId2(this.regionId2)
        .memberStatus(MemberStatus.ACTIVE).build();
  }
}
