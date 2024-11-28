package com.gogym.member.dto;

import com.gogym.member.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthRequest {

    // 로그인 요청
    @Email
    @NotBlank
    private String email; // 이메일

    @NotBlank
    private String password; // 비밀번호

    private String name; // 이름
    private String nickname; // 닉네임
    private String phone; // 핸드폰 번호
    private Role role; // 역할
    private String profileImageUrl; // 프로필 이미지 URL
    private String interestArea1; // 관심 지역 1
    private String interestArea2; // 관심 지역 2

    private String newPassword; // 새 비밀번호 (비밀번호 재설정용)
}
