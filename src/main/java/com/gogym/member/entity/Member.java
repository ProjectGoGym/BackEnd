package com.gogym.member.entity;

import com.gogym.common.entity.BaseEntity;
import com.gogym.gympay.entity.GymPay;
import com.gogym.gympay.entity.Payment;
import com.gogym.gympay.entity.Transaction;
import com.gogym.member.type.MemberStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Member extends BaseEntity {

  @Enumerated(EnumType.STRING)
  @Column(name = "member_status", nullable = false)
  @Setter
  private MemberStatus memberStatus;

  @Column(name = "member_name", nullable = false)
  private String name;

  @Column(name = "email", nullable = false, unique = true)
  private String email;

  @Column(name = "nickname", nullable = false, unique = true)
  private String nickname;

  @Column(name = "phone", nullable = false)
  private String phone;

  @Setter
  @Column(name = "password", nullable = false)
  private String password;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.VARCHAR)
  @Column(name = "role", nullable = false)
  private Role role;

  @Column(name = "profile_image_url")
  @Setter
  private String profileImageUrl;

  @Column(name = "region_id_1", nullable = true)
  private Long regionId1;

  @Column(name = "region_id_2", nullable = true)
  private Long regionId2;

  @Setter
  @Column(name = "verified_at")
  private LocalDateTime verifiedAt; // 이메일 인증 시간

  @Setter
  @Column(name = "is_kakao")
  private boolean isKakao = false; // 카카오 로그인 여부

  @Setter
  @OneToOne(mappedBy = "member", cascade = CascadeType.PERSIST)
  private GymPay gymPay;

  // 이메일 인증 여부 확인 메서드
  public boolean isVerified() {
    return this.verifiedAt != null;
  }

  // 프로필 업데이트 메서드
  public void updateProfile(String name, String nickname, String phone, String profileImageUrl,
      Long regionId1, Long regionId2) {
    this.name = name;
    this.nickname = nickname;
    this.phone = phone;
    this.profileImageUrl = profileImageUrl;
    this.regionId1 = regionId1;
    this.regionId2 = regionId2;
  }

  // 정보 마스킹 메서드
  public void masking() {
    this.name = maskString(this.name);
    this.nickname = maskString(this.nickname);
    this.email = maskEmail(this.email);
  }

  private String maskString(String input) {
    if (input == null || input.isEmpty()) {
      return input;
    }
    StringBuilder masked = new StringBuilder(input);
    for (int i = 0; i < input.length(); i++) {
      if (i % 2 == 1) {
        masked.setCharAt(i, '*');
      }
    }
    return masked.toString();
  }

  private String maskEmail(String email) {
    if (email == null || email.isEmpty()) {
      return email;
    }
    String[] parts = email.split("@");
    if (parts.length != 2) {
      return email;
    }
    parts[0] = maskString(parts[0]);
    return String.join("@", parts);
  }

  // 정보 마스킹 및 민감 정보 초기화
  public void maskSensitiveInfo(String maskedName, String maskedNickname, String maskedEmail) {
    this.name = maskedName;
    this.nickname = maskedNickname;
    this.email = maskedEmail;
    this.phone = "010-****-****";
    this.profileImageUrl = null;
  }
}
