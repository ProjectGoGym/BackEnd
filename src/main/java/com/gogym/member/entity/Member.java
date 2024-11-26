package com.gogym.member.entity;

import com.gogym.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
/**
 * 회원 엔티티
 */
public class Member extends BaseEntity {

  @Column(nullable = false)
  private String name;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false, unique = true)
  private String nickname;

  @Column(nullable = false)
  private String phone;

  @Column(nullable = false)
  private String password;
  
  @Column(nullable = false)
  private String role = "USER";

  private String profileImageUrl;
  private String interestArea1;
  private String interestArea2;
  
  private boolean isEmailVerified = false; // 이메일 인증 여부

  private String emailVerificationToken; // 인증 토큰 저장
}
