package com.gogym.member.service;

import static com.gogym.exception.ErrorCode.UNAUTHORIZED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.gogym.exception.CustomException;
import com.gogym.exception.ErrorCode;
import com.gogym.member.dto.ResetPasswordRequest;
import com.gogym.member.dto.SignInRequest;
import com.gogym.member.dto.SignUpRequest;
import com.gogym.member.entity.Member;
import com.gogym.member.entity.Role;
import com.gogym.member.jwt.JwtTokenProvider;
import com.gogym.member.repository.MemberRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import redis.embedded.RedisServer;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock
  private MemberRepository memberRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private JwtTokenProvider jwtTokenProvider;

  @Mock
  private MemberService memberService;

  @Mock
  private RedisTemplate<String, String> redisTemplate;

  @Mock
  private EmailService emailService;

  @InjectMocks
  private AuthService authService;

  private Member member;
  private SignUpRequest signUpRequest;
  private SignInRequest signInRequest;
  private ResetPasswordRequest resetPasswordRequest;
  private static RedisServer redisServer;

  @BeforeEach
  void setUp() {
    member = Member.builder().id(1L).email("0123@example.com").password("encodedPassword")
        .role(Role.USER).verifiedAt(LocalDateTime.now()).build();

    signUpRequest = SignUpRequest.builder().email("0123@example.com").password("TestPassword123!")
        .name("aaa").nickname("tomato").phone("010-1234-5678").build();

    signInRequest = new SignInRequest("0123@example.com", "TestPassword123!");
    resetPasswordRequest = ResetPasswordRequest.builder().email("0123@example.com")
        .newPassword("NewPassword123!").build();
  }

  @BeforeAll
  static void startRedis() {
    redisServer = new RedisServer(6380); // 로컬
    redisServer.start();
  }

  @AfterAll
  static void stopRedis() {
    if (redisServer != null) {
      redisServer.stop(); // 중지
    }
  }

  @Test
  void 회원가입이_성공한다() {
    // Mocking 설정
    when(passwordEncoder.encode(signUpRequest.getPassword())).thenReturn("encodedPassword");
    doNothing().when(emailService).validateEmail(signUpRequest.getEmail());

    // 메서드 호출
    authService.signUp(signUpRequest);

    // 검증
    verify(emailService).validateEmail(signUpRequest.getEmail());
    verify(memberRepository).save(any(Member.class));
  }


  @Test
  void 이미_존재하는_닉네임으로_중복확인을_시도하면_예외가_발생한다() {
    when(memberRepository.existsByNickname(signUpRequest.getNickname())).thenReturn(true);

    CustomException e = assertThrows(CustomException.class,
        () -> authService.validateNickname(signUpRequest.getNickname()));

    assertEquals(e.getErrorCode(), ErrorCode.DUPLICATE_NICKNAME);
  }

  @Test
  void 존재하지_않는_닉네임으로_중복확인을_시도하면_예외가_발생하지_않는다() {
    when(memberRepository.existsByNickname(signUpRequest.getNickname())).thenReturn(false);

    authService.validateNickname(signUpRequest.getNickname());
  }

  @Test
  void 이미_존재하는_이메일로_중복확인을_시도하면_예외가_발생한다() {
    doThrow(new CustomException(ErrorCode.DUPLICATE_EMAIL)).when(emailService)
        .validateEmail(signUpRequest.getEmail());

    CustomException e = assertThrows(CustomException.class,
        () -> emailService.validateEmail(signUpRequest.getEmail()));

    assertNotNull(e);
    assertEquals(ErrorCode.DUPLICATE_EMAIL, e.getErrorCode());
  }

  @Test
  void 존재하지_않는_이메일로_중복확인을_시도하면_예외가_발생하지_않는다() {
    emailService.validateEmail("new@example.com");
  }

  @Test
  void 잘못된_비밀번호로_로그인하면_예외가_발생한다() {
    Member mockMember = mock(Member.class);
    when(mockMember.isVerified()).thenReturn(true);
    when(mockMember.getPassword()).thenReturn(member.getPassword());

    when(memberService.findByEmail(signInRequest.getEmail())).thenReturn(mockMember);
    when(passwordEncoder.matches(signInRequest.getPassword(), mockMember.getPassword()))
        .thenReturn(false);

    CustomException e = assertThrows(CustomException.class, () -> authService.login(signInRequest));

    assertEquals(e.getErrorCode(), UNAUTHORIZED);
  }

  @Test
  void 비밀번호_재설정이_성공한다() {
    // Mock HttpServletRequest 생성
    HttpServletRequest mockRequest = mock(HttpServletRequest.class);

    // Mock Token 설정
    String mockToken = "mockToken";
    when(jwtTokenProvider.extractToken(mockRequest)).thenReturn(mockToken);
    when(jwtTokenProvider.getAuthentication(mockToken)).thenReturn(mock(Authentication.class));
    when(jwtTokenProvider.getAuthentication(mockToken).getName())
        .thenReturn(resetPasswordRequest.getEmail());

    // Mock MemberService 설정
    when(memberService.findByEmail(resetPasswordRequest.getEmail())).thenReturn(member);

    // 메서드 호출
    authService.resetPassword(mockRequest, resetPasswordRequest);

    // 검증
    verify(memberService).findByEmail(resetPasswordRequest.getEmail());
    verify(memberRepository).save(any(Member.class));
  }


  @Test
  void 로그아웃이_성공한다() {
    // Mock HttpServletRequest 생성
    HttpServletRequest mockRequest = mock(HttpServletRequest.class);

    // Mock Token 설정
    String mockToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";

    // Mock jwtTokenProvider 동작 설정
    when(jwtTokenProvider.extractToken(mockRequest)).thenReturn(mockToken);
    when(jwtTokenProvider.validateToken(mockToken)).thenReturn(true);

    // AuthService 로그아웃 호출
    authService.logout(mockRequest);

    // 추가 검증
    verify(jwtTokenProvider).extractToken(mockRequest);
    verify(jwtTokenProvider).validateToken(mockToken);
  }

  @Test
  void 인증된_이메일과_요청된_이메일이_다르면_예외가_발생한다() throws Exception {
    // Reflection을 이용한 private 메서드 접근
    Method method = AuthService.class.getDeclaredMethod("validateAuthenticatedEmail", String.class,
        String.class);
    method.setAccessible(true); // private 메서드 접근 허용

    InvocationTargetException exception = assertThrows(InvocationTargetException.class,
        () -> method.invoke(authService, "authenticated@example.com", "requested@example.com"));

    // 예외를 추출
    Throwable cause = exception.getCause();
    assertTrue(cause instanceof CustomException);
    assertEquals(ErrorCode.FORBIDDEN, ((CustomException) cause).getErrorCode());
  }

  @Test
  void JWT에서_인증된_이메일을_추출한다() {
    HttpServletRequest mockRequest = mock(HttpServletRequest.class);
    Authentication mockAuthentication = mock(Authentication.class);

    when(jwtTokenProvider.extractToken(mockRequest)).thenReturn("mockToken");
    when(jwtTokenProvider.getAuthentication("mockToken")).thenReturn(mockAuthentication);
    when(mockAuthentication.getName()).thenReturn("authenticated@example.com");

    String authenticatedEmail = authService.extractAuthenticatedEmail(mockRequest);

    assertEquals("authenticated@example.com", authenticatedEmail);
  }

  @Test
  void 특정_이메일로_사용자를_조회한다() {
    when(memberService.findByEmail(member.getEmail())).thenReturn(member);

    Member foundMember = authService.getMemberByEmail(member.getEmail());

    assertNotNull(foundMember);
    assertEquals(member.getEmail(), foundMember.getEmail());
  }

  @Test
  void 특정_이메일로_비밀번호를_업데이트한다() throws Exception {
    when(memberService.findByEmail(member.getEmail())).thenReturn(member);

    // Reflection으로 private 메서드 접근
    Method method =
        AuthService.class.getDeclaredMethod("updatePassword", String.class, String.class);
    method.setAccessible(true); // private 메서드 접근 허용

    method.invoke(authService, member.getEmail(), "UpdatedPassword!");

    verify(memberService).findByEmail(member.getEmail());
    assertEquals(passwordEncoder.encode("UpdatedPassword!"), member.getPassword());
  }
}

