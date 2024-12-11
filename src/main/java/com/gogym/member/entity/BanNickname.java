package com.gogym.member.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ban_nickname")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class BanNickname {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ban_nickname_id")
  private Long id;

  @Column(name = "banned_nickname", nullable = false)
  private String bannedNickname;
}
