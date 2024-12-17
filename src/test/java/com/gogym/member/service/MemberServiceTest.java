package com.gogym.member.service;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import com.gogym.exception.CustomException;
import com.gogym.member.dto.MemberProfileResponse;
import com.gogym.member.dto.UpdateMemberRequest;
import com.gogym.member.entity.Member;
import com.gogym.member.repository.BanNicknameRepository;
import com.gogym.member.repository.MemberRepository;
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

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testGetMyProfileById() {
    Long memberId = 1L;

    // GymPay를 Mock 객체로 생성
    GymPay gymPay = mock(GymPay.class);
    when(gymPay.getBalance()).thenReturn(10000L); // Long 타입으로 명시적 변경

    Member member =
        Member.builder().id(memberId).email("test@example.com").name("John Doe").nickname("johndoe")
            .phone("01012345678").profileImageUrl("profile.jpg").gymPay(gymPay).build();

    when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

    MemberProfileResponse response = memberService.getMyProfileById(memberId);

    assertThat(response.gymPayBalance()).isEqualTo(10000L); // Long 타입으로 확인
    verify(memberRepository).findById(memberId);
  }

  @Test
  void testUpdateMyProfileById() {
    Long memberId = 1L;

    // UpdateMemberRequest를 Mock 객체로 생성
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
  void testDeactivateMyAccountById() {
    Long memberId = 1L;
    Member member = mock(Member.class);
    when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

    memberService.deactivateMyAccountById(memberId);

    verify(member).deactivate(banNicknameRepository);
    verify(memberRepository).findById(memberId);
  }
}

