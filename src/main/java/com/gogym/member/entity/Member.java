package com.gogym.member.entity;

import com.gogym.common.entity.BaseEntity;
import com.gogym.gympay.entity.GymPay;
import com.gogym.gympay.entity.Payment;
import com.gogym.gympay.entity.Transaction;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import com.gogym.member.type.MemberStatus;
import jakarta.persistence.*;
import lombok.*;
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
  @Setter
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
  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "gym_pay_id", referencedColumnName = "id")
  private GymPay gymPay;

  @Setter
  @Column(name = "verified_at")
  private LocalDateTime verifiedAt; // 이메일 인증 시간

  @Column(name = "is_kakao", nullable = false)
  @Builder.Default
  private Boolean isKakao = false; // 카카오 로그인 여부

  @OneToMany(mappedBy = "member", cascade = CascadeType.PERSIST)
  private final List<Payment> payments = new ArrayList<>();

  @OneToMany(mappedBy = "seller")
  private final List<Transaction> salesTransactions = new ArrayList<>();

  @OneToMany(mappedBy = "buyer")
  private final List<Transaction> purchaseTransactions = new ArrayList<>();

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

}
