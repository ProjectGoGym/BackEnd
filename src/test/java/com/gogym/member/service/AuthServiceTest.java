package com.gogym.member.service;

import static com.gogym.exception.ErrorCode.UNAUTHORIZED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gogym.exception.CustomException;
import com.gogym.exception.ErrorCode;
import com.gogym.member.dto.ResetPasswordRequest;
import com.gogym.member.dto.SignInRequest;
import com.gogym.member.dto.SignUpRequest;
import com.gogym.member.entity.Member;
import com.gogym.member.jwt.JwtTokenProvider;
import com.gogym.member.repository.BanNicknameRepository;
import com.gogym.member.repository.MemberRepository;
import com.gogym.util.RedisService;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import io.jsonwebtoken.Claims;
import java.util.List;

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

  @Mock
  private RedisService redisService;

  @Mock
  private BanNicknameRepository banNicknameRepository;

  private SignUpRequest signUpRequest;
  private SignInRequest signInRequest;
  private ResetPasswordRequest resetPasswordRequest;

  @BeforeEach
  void setUp() {
    signUpRequest = SignUpRequest.builder().email("0123@example.com").password("TestPassword123!")
        .name("aaa").nickname("tomato").phone("010-1234-5678").build();

    signInRequest = new SignInRequest("0123@example.com", "TestPassword123!");
    resetPasswordRequest = ResetPasswordRequest.builder().email("0123@example.com")
        .newPassword("NewPassword123!").build();
  }

  @BeforeAll
  static void startRedis() {

  }

  @AfterAll
  static void stopRedis() {

  }

  @Test
  void 회원가입이_성공한다() {
    when(passwordEncoder.encode(signUpRequest.getPassword())).thenReturn("encodedPassword");
    doNothing().when(emailService).validateEmail(signUpRequest.getEmail());

    authService.signUp(signUpRequest, false);

    verify(emailService).validateEmail(signUpRequest.getEmail());
    verify(memberRepository).save(any(Member.class));
  }


  @Test
  void 이미_존재하는_닉네임으로_중복확인을_시도하면_예외가_발생한다() {
    when(memberRepository.existsByNickname(signUpRequest.getNickname())).thenReturn(true);

    CustomException e = assertThrows(CustomException.class,
        () -> authService.validateNickname(signUpRequest.getNickname()));

    assertEquals(ErrorCode.DUPLICATE_NICKNAME, e.getErrorCode());
  }

  @Test
  void 존재하지_않는_닉네임으로_중복확인을_시도하면_예외가_발생하지_않는다() {
    when(banNicknameRepository.existsByBannedNickname(anyString())).thenReturn(false);
    when(memberRepository.existsByNickname(anyString())).thenReturn(false);

    authService.validateNickname(signUpRequest.getNickname());

    verify(banNicknameRepository).existsByBannedNickname(anyString());
    verify(memberRepository).existsByNickname(anyString());
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
    when(mockMember.getPassword()).thenReturn("encodedPassword");
    when(memberService.findByEmail(signInRequest.getEmail())).thenReturn(mockMember);
    when(passwordEncoder.matches(signInRequest.getPassword(), "encodedPassword")).thenReturn(false);

    CustomException e = assertThrows(CustomException.class, () -> authService.login(signInRequest));
    assertEquals(ErrorCode.UNAUTHORIZED, e.getErrorCode());
  }


  void 비밀번호_재설정이_성공한다() {
    HttpServletRequest mockRequest = mock(HttpServletRequest.class);
    String mockToken = "mockToken";
    Member mockMember = mock(Member.class);

    // Mocking 요청 데이터 설정
    ResetPasswordRequest resetPasswordRequest =
        ResetPasswordRequest.builder().email("test@example.com").currentPassword("testPassword")
            .newPassword("newPassword").build();

    // Mocking 및 스터빙
    when(jwtTokenProvider.extractToken(mockRequest)).thenReturn(mockToken);
    when(jwtTokenProvider.getAuthentication(mockToken)).thenReturn(mock(Authentication.class));
    when(jwtTokenProvider.getAuthentication(mockToken).getName())
        .thenReturn(resetPasswordRequest.getEmail());
    when(memberService.findByEmail(resetPasswordRequest.getEmail())).thenReturn(mockMember);
    when(mockMember.getPassword()).thenReturn("encodedPassword");
    when(passwordEncoder.matches(anyString(), eq("encodedPassword"))).thenReturn(true);

    // 테스트 실행
    authService.resetPassword(mockRequest, resetPasswordRequest);

    verify(memberService).findByEmail(resetPasswordRequest.getEmail());
    verify(passwordEncoder).matches(eq("testPassword"), eq("encodedPassword"));
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
    method.setAccessible(true);

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
    Member mockMember = mock(Member.class);
    when(mockMember.getEmail()).thenReturn("test@example.com");
    when(memberService.findByEmail(mockMember.getEmail())).thenReturn(mockMember);

    Member foundMember = authService.getMemberByEmail(mockMember.getEmail());

    assertNotNull(foundMember);
    assertEquals(mockMember.getEmail(), foundMember.getEmail());
  }

  @Test
  void 특정_이메일로_비밀번호를_업데이트한다() {
    HttpServletRequest mockRequest = mock(HttpServletRequest.class);
    String mockToken = "mockToken";

    Member mockMember =
        Member.builder().email("test@example.com").password("encodedPassword").build();

    ResetPasswordRequest resetPasswordRequest =
        ResetPasswordRequest.builder().email("test@example.com").currentPassword("testPassword")
            .newPassword("newPassword").build();

    // Mock 설정
    when(jwtTokenProvider.extractToken(mockRequest)).thenReturn(mockToken);
    when(jwtTokenProvider.getAuthentication(mockToken)).thenReturn(mock(Authentication.class));
    when(jwtTokenProvider.getAuthentication(mockToken).getName()).thenReturn("test@example.com");
    when(memberService.findByEmail("test@example.com")).thenReturn(mockMember);
    when(passwordEncoder.matches(eq("testPassword"), eq("encodedPassword"))).thenReturn(true);

    // 테스트 실행
    authService.resetPassword(mockRequest, resetPasswordRequest);

    verify(memberRepository).save(any(Member.class));
  }

 /*
  void 카카오회원가입이_성공한다() {
    when(passwordEncoder.encode(signUpRequest.getPassword())).thenReturn("encodedPassword");
    doNothing().when(emailService).validateEmail(signUpRequest.getEmail());

    // isKakao 값을 true로 설정하여 호출
    authService.signUp(signUpRequest, true);

    verify(emailService).validateEmail(signUpRequest.getEmail());
    verify(memberRepository).save(any(Member.class));
    verify(memberRepository).findByEmail(signUpRequest.getEmail());
  }*/
}
