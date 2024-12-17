package com.gogym.member.entity;

import com.gogym.common.entity.BaseEntity;
import com.gogym.gympay.entity.GymPay;
import com.gogym.member.repository.BanNicknameRepository;
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
  private String memberStatus;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "member_id")
  private Long id;

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

  @OneToOne(mappedBy = "member", cascade = CascadeType.ALL)
  private GymPay gymPay;

  @Setter
  @Column(name = "verified_at")
  private LocalDateTime verifiedAt; // 이메일 인증 시간을 저장

  @Column(name = "is_active", nullable = false)
  private boolean isActive = true;

  private boolean isKakao;

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

  // 탈퇴 처리 메서드
  public void deactivate(BanNicknameRepository banNicknameRepository) {
    this.isActive = false;

    // 이름과 닉네임 마스킹
    this.name = maskString(this.name);
    this.nickname = maskString(this.nickname);

    // 이메일 마스킹
    this.email = maskEmail(this.email);

    // BanNickname 테이블에 저장
    if (banNicknameRepository != null) {
      BanNickname banNickname = BanNickname.builder().bannedNickname(this.nickname).build();
      banNicknameRepository.save(banNickname);
    }

    this.phone = null;
    this.profileImageUrl = null;
  }

  // 문자열 마스킹 (짝수 인덱스 문자만 '*')
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

  // 이메일 마스킹
  private String maskEmail(String email) {
    if (email == null || email.isEmpty()) {
      return email;
    }
    String[] parts = email.split("@");
    if (parts.length != 2) {
      return email; // 유효하지 않은 이메일
    }
    parts[0] = maskString(parts[0]); // 아이디 부분 마스킹
    return String.join("@", parts);
  }

  public String getMemberStatus() {
    return memberStatus;
  }

  public void setMemberStatus(String memberStatus) {
    this.memberStatus = memberStatus;
  }

  public GymPay getGymPay() {
    return this.gymPay;
  }

  public void setGymPay(GymPay gymPay) {
    this.gymPay = gymPay;
  }

  public static class MemberBuilder {
    public MemberBuilder isKakao(boolean isKakao) {
      this.isKakao = isKakao;
      return this;
    }
  }
}
