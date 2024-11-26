package com.gogym.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

//사용자 계정 및 인증 관련 요청 및 응답 데이터 객체

public class MemberDto {

  //회원가입 요청

  @Getter
  @Setter
  public static class SignUpRequest {

    //사용자 이름
    @NotBlank
    private String name;

    //이메일 주소
    @Email
    @NotBlank
    private String email;

    // 닉네임
    @NotBlank
    private String nickname;

    // 핸드폰 번호
    @NotBlank
    private String phone;

    // 비밀번호
    @NotBlank
    private String password;

    // 사용자 역할
    @NotBlank
    @Pattern(regexp = "^(USER|ADMIN)$")
    private String role;

    // 프로필 이미지 url
    private String profileImageUrl;

    //관심지역 1 and 2
    private String interestArea1;
    private String interestArea2;
  }

  //로그인 요청 데이터

  @Data
  public static class LoginRequest {

    //이메일 주소
    @Email
    @NotBlank
    private String email;

    //비밀번호
    @NotBlank
    private String password;
  }

  //회원가입 응답 데이터

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class SignUpResponse {
    private Long userId;    // 사용자 ID
    private String nickname; // 사용자 닉네임
    private String email;    // 사용자 이메일
  }
  
  //로그인 응답
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class LoginResponse {
    private Long userId;          // 사용자 ID
    private String nickname;      // 닉네임
    private String email;         // 이메일
    private String token;         // JWT 토큰
    private String role;          // 사용자 역할 ("USER" 또는 "ADMIN")
  }

  //비밀번호 재설정 요청
  @Data
  public static class ResetPasswordRequest {

    //이메일 주소
    @Email
    @NotBlank
    private String email;

    //새로운 비밀번호
    @NotBlank
    private String newPassword;
    
    //새로운 비밀번호 확인
    @NotBlank
    private String confirmPassword; 
  }
}



