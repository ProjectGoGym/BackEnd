package com.gogym.member.service;

import com.gogym.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CheckNicknameService {

  private final MemberRepository memberRepository;

  public boolean checkNickname(String nickname) {
    return !memberRepository.existsByNickname(nickname); // true 면 닉네임 사용 가능
  }
}
