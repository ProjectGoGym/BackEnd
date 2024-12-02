package com.gogym.member.service;

import com.gogym.exception.CustomException;
import com.gogym.exception.ErrorCode;
import com.gogym.member.entity.Member;
import com.gogym.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    // 사용자 조회하는 메서드 (이메일로)
    public Member findByEmail(String email) {
      return memberRepository.findByEmail(email)
          .orElseThrow(() -> new CustomException(ErrorCode.EMAIL_NOT_FOUND));
      }
    // 사용자 조회하는 메서드 (아이디로)
    public Member findById(Long id) {
      return memberRepository.findById(id)
          .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
      }
}

