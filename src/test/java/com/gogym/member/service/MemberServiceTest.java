package com.gogym.member.service;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import com.gogym.exception.CustomException;
import com.gogym.member.dto.MemberProfileResponse;
import com.gogym.member.dto.UpdateMemberRequest;
import com.gogym.member.entity.Member;
import com.gogym.member.repository.BanNicknameRepository;
import com.gogym.member.repository.MemberRepository;
import com.gogym.member.type.MemberStatus;
import com.gogym.gympay.entity.GymPay;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

class MemberServiceTest {

  @Mock
  private MemberRepository memberRepository;

  @Mock
  private BanNicknameRepository banNicknameRepository;

  @InjectMocks
  private MemberService memberService;
  @Mock
  private Member member;

  @BeforeEach
  void 설정() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void 마이페이지_조회_성공() {
    Long memberId = 1L;

    GymPay gymPay = mock(GymPay.class);
    when(gymPay.getBalance()).thenReturn(10000L);

    member = mock(Member.class);
    when(member.getId()).thenReturn(1L);
    when(member.getEmail()).thenReturn("test@example.com");
    when(member.getName()).thenReturn("John Doe");
    when(member.getNickname()).thenReturn("johndoe");
    when(member.getPhone()).thenReturn("01012345678");
    when(member.getProfileImageUrl()).thenReturn("profile.jpg");
    when(member.getGymPay()).thenReturn(gymPay);

    when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

    MemberProfileResponse response = memberService.getMyProfileById(memberId);

    assertThat(response.gymPayBalance()).isEqualTo(10000L);
    verify(memberRepository).findById(memberId);
  }

  @Test
  void 마이페이지_수정_성공() {
    Long memberId = 1L;

    UpdateMemberRequest request = mock(UpdateMemberRequest.class);
    when(request.name()).thenReturn("New Name");
    when(request.nickname()).thenReturn("newNick");
    when(request.phone()).thenReturn("01099999999");
    when(request.profileImageUrl()).thenReturn("newImage.jpg");

    Member member = mock(Member.class);
    when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

    memberService.updateMyProfileById(memberId, request);

    verify(member).updateProfile(request.name(), request.nickname(), request.phone(),
        request.profileImageUrl());
    verify(memberRepository).findById(memberId);
  }

  @Test
  void 회원_탈퇴_성공() {
    Long memberId = 1L;

    Member member = mock(Member.class);
    when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

    memberService.deactivateMyAccountById(memberId);

    verify(member).setMemberStatus(MemberStatus.DEACTIVATED);
    verify(memberRepository).findById(memberId);
  }
}

