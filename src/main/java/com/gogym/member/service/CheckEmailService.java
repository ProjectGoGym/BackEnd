package com.gogym.member.service;

import com.gogym.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CheckEmailService {

  private final MemberRepository memberRepository;

  public boolean checkEmail(String email) {
    return !memberRepository.existsByEmail(email); // true 면 email 사용가능
  }
}
