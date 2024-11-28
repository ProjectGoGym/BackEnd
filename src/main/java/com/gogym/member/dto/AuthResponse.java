package com.gogym.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private Long userId; // 유저 ID
    private String nickname; // 닉네임
    private String email; // 이메일

    private String token; // 인증 토큰
}
