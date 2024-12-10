package com.gogym.gympay.service;

import com.gogym.exception.CustomException;
import com.gogym.exception.ErrorCode;
import com.gogym.gympay.entity.GymPay;
import com.gogym.gympay.repository.GymPayRepository;
import com.gogym.member.entity.Member;
import com.gogym.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GymPayService {

  private final MemberService memberService;
  private final GymPayRepository gymPayRepository;

  @Transactional
  public Long save(Long memberId) {
    Member member = memberService.findById(memberId);
    GymPay gymPay = new GymPay(0, member);

    gymPayRepository.save(gymPay);

    return gymPay.getId();
  }

  @Transactional
  public void updateBalance(long memberId, int amount) {
    Member member = memberService.findById(memberId);
    GymPay gymPay = getByMember(member);

    gymPay.charge(amount);
  }

  private GymPay getByMember(Member member) {
    return gymPayRepository.findByMember(member)
        .orElseThrow(() -> new CustomException(ErrorCode.GYM_PAY_NOT_FOUND));
  }
}
