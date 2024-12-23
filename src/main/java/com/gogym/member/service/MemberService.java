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
import com.gogym.member.type.MemberStatus;
import com.gogym.region.dto.RegionResponseDto;
import com.gogym.region.service.RegionService;
import lombok.RequiredArgsConstructor;
import com.gogym.member.entity.BanNickname;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

  private final MemberRepository memberRepository;
  private final BanNicknameRepository banNicknameRepository;
  private final RegionService regionService;

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
    Long gymPayId = (member.getGymPay() != null) ? member.getGymPay().getId() : null;
    long gymPayBalance = (member.getGymPay() != null) ? member.getGymPay().getBalance() : 0L;

    String regionName1 = null;
    String regionName2 = null;

    if (member.getRegionId1() != null) {
      RegionResponseDto region1 = regionService.findById(member.getRegionId1());
      regionName1 = (region1.city() != null ? region1.city() + " - " : "") + region1.district();
    }

    if (member.getRegionId2() != null) {
      RegionResponseDto region2 = regionService.findById(member.getRegionId2());
      regionName2 = (region2.city() != null ? region2 + " - " : "") + region2.district();
    }

    return new MemberProfileResponse(member.getId(), member.getEmail(), member.getName(),
        member.getNickname(), member.getPhone(), member.getProfileImageUrl(), gymPayBalance,
        gymPayId, member.getRegionId1(), member.getRegionId2(), regionName1, regionName2);
  }

  // 마이페이지 수정
  @Transactional
  public void updateMyProfileById(Long memberId, UpdateMemberRequest request) {
    Member member = findById(memberId);
    member.updateProfile(request.name(), request.nickname(), request.phone(),
        request.profileImageUrl(), request.regionId1(), request.regionId2());
  }

  // 회원 탈퇴 (소프트)
  @Transactional
  public void deactivateMyAccountById(Long memberId) {
    Member member = findById(memberId);

    // BanNickname 저장을 위한 원본 닉네임 저장
    String originalNickname = member.getNickname();
    banNicknameRepository.save(new BanNickname(originalNickname));

    member.setMemberStatus(MemberStatus.DEACTIVATED); // 상태 변경

    // 이름, 닉네임, 이메일 마스킹 및 민감 정보 초기화(엔티티에서 수정)
    String maskedName = maskString(member.getName());
    String maskedNickname = maskString(member.getName());
    String maskedEmail = maskEmail(member.getEmail());
    member.maskSensitiveInfo(maskedName, maskedNickname, maskedEmail);

    memberRepository.save(member);
  }

  // 문자열 마스킹 (짝수 인덱스 문자만 '*')
  private String maskString(String input) {
    if (input == null || input.isEmpty()) {
      return input;
    }
    StringBuilder masked = new StringBuilder(input);
    for (int i = 0; i < input.length(); i++) {
      if (i % 2 == 1) {
        masked.setCharAt(i, '*');
      }
    }
    return masked.toString();
  }

  // 이메일 마스킹
  private String maskEmail(String email) {
    if (email == null || email.isEmpty()) {
      return email;
    }
    String[] parts = email.split("@");
    if (parts.length != 2) {
      return email;
    }
    parts[0] = maskString(parts[0]);
    return String.join("@", parts);
  }
}


