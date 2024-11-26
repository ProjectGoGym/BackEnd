package com.gogym.member.service;

import com.gogym.common.response.ErrorCode;
import com.gogym.exception.CustomException;
import com.gogym.member.dto.SignUpRequest;
import com.gogym.member.dto.SignUpResponse;
import com.gogym.member.entity.Member;
import com.gogym.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SignUpService {

  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;

  public SignUpResponse signUp(SignUpRequest request) {
    if (memberRepository.existsByEmail(request.getEmail())) {
      throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
    }

    if (memberRepository.existsByNickname(request.getNickname())) {
      throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
    }

    Member member = Member.builder()
      .name(request.getName())
      .email(request.getEmail())
      .nickname(request.getNickname())
      .phone(request.getPhone())
      .password(passwordEncoder.encode(request.getPassword()))
      .role(request.getRole())
      .profileImageUrl(request.getProfileImageUrl())
      .interestArea1(request.getInterestArea1())
      .interestArea2(request.getInterestArea2())
      .build();

    memberRepository.save(member);

    return new SignUpResponse(
      member.getId(),
      member.getNickname(),
      member.getEmail()
    );
  }
}
