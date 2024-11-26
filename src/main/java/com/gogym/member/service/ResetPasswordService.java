package com.gogym.member.service;

import com.gogym.common.response.ErrorCode;
import com.gogym.exception.CustomException;
import com.gogym.member.dto.ResetPasswordRequest;
import com.gogym.member.entity.Member;
import com.gogym.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResetPasswordService {

  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;

  public void resetPassword(ResetPasswordRequest request) {
    // 비밀번호 일치 확인
    if (!request.getNewPassword().equals(request.getConfirmPassword())) {
      throw new CustomException(ErrorCode.PASSWORDS_DO_NOT_MATCH);
    }

    // 이메일로 사용자 찾기
    Member member = memberRepository.findByEmail(request.getEmail())
      .orElseThrow(() -> new CustomException(ErrorCode.EMAIL_NOT_FOUND));

    // 새 비밀번호 암호화 후 저장
    member.setPassword(passwordEncoder.encode(request.getNewPassword()));
    memberRepository.save(member);
  }
}
