package com.gogym.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gogym.exception.CustomException;
import com.gogym.exception.ErrorCode;
import com.gogym.member.dto.MemberProfileResponse;
import com.gogym.member.dto.UpdateMemberRequest;
import com.gogym.member.entity.Member;
import com.gogym.member.repository.BanNicknameRepository;
import com.gogym.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

  private final MemberRepository memberRepository;
  private final BanNicknameRepository banNicknameRepository;

  // 이메일로 사용자 조회
  public Member findByEmail(String email) {
    return memberRepository.findByEmail(email)
        .orElseThrow(() -> new CustomException(ErrorCode.EMAIL_NOT_FOUND));
  }

  // ID로 사용자 조회
  public Member findById(Long id) {
    return memberRepository.findById(id)
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
  }

  // 마이페이지 조회
  public MemberProfileResponse getMyProfileById(Long memberId) {
    Member member = findById(memberId);
    long gymPayBalance = (member.getGymPay() != null) ? member.getGymPay().getBalance() : 0L;

    return new MemberProfileResponse(member.getId(), member.getEmail(), member.getName(),
        member.getNickname(), member.getPhone(), member.getProfileImageUrl(), gymPayBalance);
  }

  // 마이페이지 수정
  @Transactional
  public void updateMyProfileById(Long memberId, UpdateMemberRequest request) {
    Member member = findById(memberId);
    member.updateProfile(request.name(), request.nickname(), request.phone(),
        request.profileImageUrl());
  }

  // 회원 탈퇴 (소프트)
  @Transactional
  public void deactivateMyAccountById(Long memberId) {
    Member member = findById(memberId);
    member.deactivate(banNicknameRepository);
  }
}


