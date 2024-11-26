package com.gogym.member.service;

import com.gogym.common.response.ErrorCode;
import com.gogym.exception.CustomException;
import com.gogym.member.entity.Member;
import com.gogym.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VerifyEmailService {

  private final MemberRepository memberRepository;

  public void verifyEmailToken(String token) {
    // 토큰으로 회원 조회
    Member member = memberRepository.findByEmailVerificationToken(token)
      .orElseThrow(() -> new CustomException(ErrorCode.INVALID_TOKEN));

    // 이메일 인증 완료 처리
    member.setEmailVerified(true);
    member.setEmailVerificationToken(null);
    memberRepository.save(member);
  }
}
