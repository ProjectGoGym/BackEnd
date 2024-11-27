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

  // 사용자 데이터를 처리하는 리포지토리
  private final MemberRepository memberRepository;

  // 비밀번호 암호화를 위한 인코더
  private final PasswordEncoder passwordEncoder;

  public SignUpResponse signUp(SignUpRequest request) {
    // 이메일 중복 확인
    if (memberRepository.existsByEmail(request.getEmail())) {
      throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
    }

    // 닉네임 중복 확인
    if (memberRepository.existsByNickname(request.getNickname())) {
      throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
    }

    // 회원 정보를 엔티티로 생성
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

    // 회원 데이터 저장
    memberRepository.save(member);

    // 응답 객체 생성 및 반환
    return new SignUpResponse(
      member.getId(),
      member.getNickname(),
      member.getEmail()
    );
  }
}

