package com.gogym.member.service;

import com.gogym.common.response.ErrorCode;
import com.gogym.config.JwtTokenProvider;
import com.gogym.exception.CustomException;
import com.gogym.member.dto.LoginRequest;
import com.gogym.member.dto.LoginResponse;
import com.gogym.member.entity.Member;
import com.gogym.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LoginService {

  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;

  public LoginResponse login(LoginRequest request) {
    Member member = memberRepository.findByEmail(request.getEmail())
      .orElseThrow(() -> new CustomException(ErrorCode.EMAIL_NOT_FOUND));

    if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
      throw new CustomException(ErrorCode.INVALID_PASSWORD);
    }

    List<String> roles = List.of(member.getRole());
    String token = jwtTokenProvider.createToken(member.getEmail(), roles);

    return LoginResponse.builder()
      .userId(member.getId())
      .nickname(member.getNickname())
      .email(member.getEmail())
      .token(token)
      .role(member.getRole())
      .build();
  }
}
