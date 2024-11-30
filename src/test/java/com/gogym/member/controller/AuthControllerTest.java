package com.gogym.member.controller;
/*
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gogym.member.dto.SignUpRequest;
import com.gogym.member.dto.SignInRequest;
import com.gogym.member.service.AuthService;
import com.gogym.member.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private AuthService authService;

  private SignUpRequest signUpRequest;
  private SignInRequest signInRequest;

  @BeforeEach
  void setUp() {
    signUpRequest = SignUpRequest.builder()
      .name("0550")
      .email("0550@example.com")
      .nickname("user0550")
      .phone("010-5678-1234")
      .password("password123")
      .role(Role.USER)
      .profileImageUrl("http://example.com/profile.jpg")
      .interestArea1("Fitness")
      .interestArea2("Yoga")
      .build();

    signInRequest = SignInRequest.builder()
      .email("0550@example.com")
      .password("password123")
      .build();
  }

  @Test
  @DisplayName("회원가입 요청을 처리하고 성공 상태를 반환한다.")
  void signUp_shouldReturnSuccessResponse() throws Exception {
    String jsonRequest = objectMapper.writeValueAsString(signUpRequest);

    mockMvc.perform(post("/api/auth/sign-up")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonRequest))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.code").value("AUTH_SUCCESS"));
  }

  @Test
  @DisplayName("로그인 요청을 처리하고 JWT 토큰을 반환한다.")
  void signIn_shouldReturnJwtToken() throws Exception {
      // Mocking AuthService
      doNothing().when(authService).login(any(SignInRequest.class));

      String jsonRequest = objectMapper.writeValueAsString(signInRequest);

      mockMvc.perform(post("/api/auth/sign-in")
              .contentType(MediaType.APPLICATION_JSON)
              .content(jsonRequest))
          .andExpect(status().isOk())
          .andExpect(header().exists(HttpHeaders.AUTHORIZATION))
          .andExpect(header().string(HttpHeaders.AUTHORIZATION, "Bearer mockJwtToken"));
  }

  @Test
  @DisplayName("이메일 중복 확인 요청을 처리한다.")
  void checkEmail_shouldReturnSuccessResponse() throws Exception {
    mockMvc.perform(get("/api/auth/check-email")
        .param("email", "0550@example.com"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true));
  }

  @Test
  @DisplayName("닉네임 중복 확인 요청을 처리한다.")
  void checkNickname_shouldReturnSuccessResponse() throws Exception {
    mockMvc.perform(get("/api/auth/check-nickname")
        .param("nickname", "user0550"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true));
  }

  @Test
  @DisplayName("비밀번호 재설정 요청을 처리한다.")
  void resetPassword_shouldReturnSuccessResponse() throws Exception {
    String jsonRequest = """
      {
        "email": "0550@example.com",
        "newPassword": "NewPassword@123"
      }
      """;

    mockMvc.perform(post("/api/auth/reset-password")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonRequest))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true));
  }

  @Test
  @DisplayName("이메일 인증 요청을 처리한다.")
  void sendVerificationEmail_shouldReturnSuccessResponse() throws Exception {
    mockMvc.perform(post("/api/auth/send-verification-email")
        .param("email", "0550@example.com"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true));
  }

  @Test
  @DisplayName("이메일 인증 확인 요청을 처리한다.")
  void verifyEmail_shouldReturnSuccessResponse() throws Exception {
    mockMvc.perform(get("/api/auth/verify-email")
        .param("token", "sampleToken"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true));
  }
}

*/