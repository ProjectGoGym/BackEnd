package com.gogym.member.service;

import static com.gogym.exception.ErrorCode.UNAUTHORIZED;
import static com.gogym.exception.ErrorCode.FORBIDDEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

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
  private EmailService emailService;
  
  @InjectMocks
  private AuthService authService;

  private Member member;
  private SignUpRequest signUpRequest;
  private SignInRequest signInRequest;
  private ResetPasswordRequest resetPasswordRequest;

  @BeforeEach
  void setUp() {
    member = Member.builder()
        .id(1L)
        .email("0123@example.com")
        .password("encodedPassword")
        .role(Role.USER)
        .build();

    signUpRequest = SignUpRequest.builder()
        .email("0123@example.com")
        .password("TestPassword123!")
        .name("aaa")
        .nickname("tomato")
        .phone("010-1234-5678")
        .build();

    signInRequest = new SignInRequest("0123@example.com", "TestPassword123!");

    resetPasswordRequest = ResetPasswordRequest.builder()
        .email("0123@example.com")
        .newPassword("NewPassword123!")
        .build();
  }

  @Test
  void 회원가입이_성공한다() {
    // given
    when(passwordEncoder.encode(signUpRequest.getPassword())).thenReturn("encodedPassword");

    // when
    authService.signUp(signUpRequest);

    // then
    verify(memberRepository).save(any(Member.class));
  }

  @Test
  void 이미_존재하는_닉네임으로_중복확인을_시도하면_예외가_발생한다() {
    // given
    when(memberRepository.existsByNickname(signUpRequest.getNickname())).thenReturn(true);

    // when & then
    CustomException e = assertThrows(CustomException.class, 
        () -> authService.validateNickname(signUpRequest.getNickname()));

    // then
    assertEquals(e.getErrorCode(), ErrorCode.DUPLICATE_NICKNAME);
  }

  @Test
  void 존재하지_않는_닉네임으로_중복확인을_시도하면_예외가_발생하지_않는다() {
    // given
    when(memberRepository.existsByNickname(signUpRequest.getNickname())).thenReturn(false);

    // when
    authService.validateNickname(signUpRequest.getNickname());
  }
  
  @Test
  void 존재하지_않는_이메일로_중복확인을_시도하면_예외가_발생하지_않는다() {
    // when
    emailService.validateEmail("new@example.com");
  }

  @Test
  void 올바른_정보로_로그인하면_성공적으로_토큰을_반환한다() {
    // given
    when(memberService.findByEmail(signInRequest.getEmail())).thenReturn(member);
    when(passwordEncoder.matches(signInRequest.getPassword(), member.getPassword())).thenReturn(true);
    when(jwtTokenProvider.createToken(eq(member.getEmail()), eq(member.getId()), any()))
      .thenReturn("testToken");

    // when
    LoginResponse response = authService.login(signInRequest);

    // then
    assertNotNull(response);
    assertEquals(response.getEmail(), member.getEmail());
  }

  @Test
  void 잘못된_비밀번호로_로그인하면_예외가_발생한다() {
    // given
    when(memberService.findByEmail(signInRequest.getEmail())).thenReturn(member);
    when(passwordEncoder.matches(signInRequest.getPassword(), member.getPassword())).thenReturn(false);

    // when
    CustomException e = assertThrows(CustomException.class, () -> authService.login(signInRequest));

    // then
    assertEquals(e.getErrorCode(), UNAUTHORIZED);
  }

  @Test
  void 비밀번호_재설정이_성공한다() {
    // given
    when(memberService.findById(member.getId())).thenReturn(member);
    when(memberService.findByEmail(resetPasswordRequest.getEmail())).thenReturn(member);

    // when
    authService.resetPassword(member.getId(), resetPasswordRequest);

    // then
    verify(memberService).findById(member.getId());
    verify(memberService).findByEmail(resetPasswordRequest.getEmail());
  }

  @Test
  void 인증된_이메일과_재설정_요청_이메일이_다르면_예외가_발생한다() {
    // given
    Member differentMember = Member.builder()
      .id(1L)
      .email("authenticated@example.com") // 인증된 이메일
      .password("encodedPassword")
      .build();

    ResetPasswordRequest differentResetPasswordRequest = ResetPasswordRequest.builder()
      .email("request@example.com") // 요청된 이메일
      .newPassword("NewPassword123!")
      .build();

    when(memberService.findById(differentMember.getId())).thenReturn(differentMember);

    // when
    CustomException e = assertThrows(CustomException.class, 
        () -> authService.resetPassword(differentMember.getId(), differentResetPasswordRequest));

    // then
    assertEquals(e.getErrorCode(), FORBIDDEN);
  }
}