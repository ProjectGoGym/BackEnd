package com.gogym.member.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "kakao_member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class KakaoMember {

  @Id
  @Column(name = "kakao_id")
  private Long kakaoId;

  @OneToOne
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @Column(nullable = false, unique = true)
  private String uuid;

}
