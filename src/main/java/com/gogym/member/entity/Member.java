package com.gogym.member.entity;

import com.gogym.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Member extends BaseEntity {

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

  @Setter
  @Column(name = "verified_at")
  private boolean verifiedAt = false;
  
  //이메일 인증 상태 업데이트 메서드
  public void verifyEmail() {
    this.verifiedAt = true; 
  }
  
  //프로필 업데이트 메서드
  public void updateProfile(String name, String nickname, String phone, String profileImageUrl) {
    this.name = name;
    this.nickname = nickname;
    this.phone = phone;
    this.profileImageUrl = profileImageUrl;
  }
}


