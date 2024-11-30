package com.gogym.member.dto;

import com.gogym.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private Long userId; // 유저 ID
    private String nickname; // 닉네임
    private String email; // 이메일

    private String token; // 인증 토큰
    
    public static AuthResponse fromEntity(Member member) {
      return AuthResponse.builder()
        .userId(member.getId())
        .nickname(member.getNickname())
        .email(member.getEmail())
        .build();
    }
    
    public static AuthResponse fromEntityWithToken(Member member, String token) {
      return AuthResponse.builder()
        .userId(member.getId())
        .nickname(member.getNickname())
        .email(member.getEmail())
        .token(token)
        .build();
    }
}

