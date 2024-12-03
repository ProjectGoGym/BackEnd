package com.gogym.member.service;

import com.gogym.exception.CustomException;
import com.gogym.exception.ErrorCode;
import com.gogym.member.dto.LoginResponse;
import com.gogym.member.dto.ResetPasswordRequest;
import com.gogym.member.dto.SignInRequest;
import com.gogym.member.dto.SignUpRequest;
import com.gogym.member.entity.Member;
import com.gogym.member.entity.Role;
import com.gogym.member.jwt.JwtTokenProvider;
import com.gogym.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

  private AuthService authService;
  private MemberRepository memberRepository;
  private PasswordEncoder passwordEncoder;
  private JwtTokenProvider jwtTokenProvider;

  @BeforeEach
  void setup() {
    memberRepository = Mockito.mock(MemberRepository.class);
    passwordEncoder = Mockito.mock(PasswordEncoder.class);
    jwtTokenProvider = Mockito.mock(JwtTokenProvider.class);
    authService = new AuthService(null, memberRepository, passwordEncoder, jwtTokenProvider, null, null);
  }

  @Test
  void 회원가입_성공() {
    SignUpRequest request = SignUpRequest.builder()
        .email("test@example.com")
        .password("password123")
        .name("Test User")
        .nickname("tester")
        .role(Role.USER)
        .build();

    when(memberRepository.existsByEmail("test@example.com")).thenReturn(false);
    when(memberRepository.existsByNickname("tester")).thenReturn(false);
    when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

    authService.signUp(request);

    verify(memberRepository, times(1)).save(any(Member.class));
  }

  @Test
  void 회원가입_중복_이메일_예외() {
    SignUpRequest request = SignUpRequest.builder()
        .email("test@example.com")
        .password("password123")
        .name("Test User")
        .nickname("tester")
        .role(Role.USER)
        .build();

    when(memberRepository.existsByEmail("test@example.com")).thenReturn(true);

    CustomException exception = assertThrows(CustomException.class, () -> authService.signUp(request));
    assertEquals(ErrorCode.DUPLICATE_EMAIL, exception.getErrorCode());
  }

  @Test
  void 로그인_성공() {
    SignInRequest request = new SignInRequest("test@example.com", "password123");
    Member member = Member.builder()
        .email("test@example.com")
        .password("encodedPassword")
        .role(Role.USER)
        .build();

    when(memberRepository.findByEmail("test@example.com")).thenReturn(Optional.of(member));
    when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
    when(jwtTokenProvider.createToken("test@example.com", List.of("USER"))).thenReturn("mockedToken");

    LoginResponse response = authService.login(request);
    
    assertEquals("mockedToken", response.getToken());
    assertEquals("test@example.com", response.getEmail());
    assertEquals("USER", member.getRole().name());
  }

  @Test
  void 로그인_비밀번호_불일치_예외() {
    SignInRequest request = new SignInRequest("test@example.com", "wrongPassword");
    Member member = Member.builder()
        .email("test@example.com")
        .password("encodedPassword")
        .role(Role.USER)
        .build();

    when(memberRepository.findByEmail("test@example.com")).thenReturn(Optional.of(member));
    when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

    CustomException exception = assertThrows(CustomException.class, () -> authService.login(request));
    assertEquals(ErrorCode.UNAUTHORIZED, exception.getErrorCode());
  }

  @Test
  void 비밀번호_재설정_성공() {
    ResetPasswordRequest request = new ResetPasswordRequest("test@example.com", "newPassword123");
    Member member = Member.builder()
        .email("test@example.com")
        .password("oldEncodedPassword")
        .build();

    String token = "mockedToken";
    Authentication authentication = mock(Authentication.class);

    when(jwtTokenProvider.getAuthentication(token)).thenReturn(authentication);
    when(authentication.getName()).thenReturn("test@example.com"); // Mock 설정 추가
    when(memberRepository.findByEmail("test@example.com")).thenReturn(Optional.of(member));
    when(passwordEncoder.encode("newPassword123")).thenReturn("newEncodedPassword");

    authService.resetPassword("Bearer mockedToken", request);

    assertEquals("newEncodedPassword", member.getPassword());
  }

}

