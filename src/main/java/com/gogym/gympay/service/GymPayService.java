package com.gogym.gympay.service;

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
  public void charge(Member member, Long amount) {
    GymPay gymPay = member.getGymPay();

    gymPay.charge(amount);
  }
}
