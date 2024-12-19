package com.gogym.member.entity;

import com.gogym.common.entity.BaseEntity;
import com.gogym.gympay.entity.GymPay;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Member extends BaseEntity {

  @Enumerated(EnumType.STRING)
  @Column(name = "member_status", nullable = false)
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
  @Column(name = "role", nullable = false)
  private Role role;

  @Column(name = "profile_image_url")
  private String profileImageUrl;

  @Column(name = "region_id_1", nullable = true)
  private Long regionId1;

  @Column(name = "region_id_2", nullable = true)
  private Long regionId2;

  @Setter
  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "gym_pay_id", referencedColumnName = "id")
  private GymPay gymPay;

  @Setter
  @Column(name = "verified_at")
  private LocalDateTime verifiedAt; // 이메일 인증 시간

  @Column(name = "is_kakao", nullable = false)
  @Builder.Default
  private boolean isKakao = false;

  // 이메일 인증 여부 확인 메서드
  public boolean isVerified() {
    return this.verifiedAt != null;
  }

  // 프로필 업데이트 메서드
  public void updateProfile(String name, String nickname, String phone, String profileImageUrl) {
    this.name = name;
    this.nickname = nickname;
    this.phone = phone;
    this.profileImageUrl = profileImageUrl;
  }

  // 상태 변경 메서드
  public void setMemberStatus(MemberStatus memberStatus) {
    this.memberStatus = memberStatus;
  }

  // 민감 정보 초기화
  public void clearSensitiveInfo() {
    this.phone = null;
    this.profileImageUrl = null;
  }

}

