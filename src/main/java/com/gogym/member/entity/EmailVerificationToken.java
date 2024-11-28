package com.gogym.member.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
public class EmailVerificationToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String token;

  @Column(nullable = false)
  private String email;

  @Column(nullable = false)
  private LocalDateTime expirationTime;

  public EmailVerificationToken(String email) {
    this.email = email;
    this.token = UUID.randomUUID().toString();
    this.expirationTime = LocalDateTime.now().plusHours(24); // 토큰 유효기간 24시간
  }

  public boolean isExpired() {
    return LocalDateTime.now().isAfter(expirationTime);
  }
}
