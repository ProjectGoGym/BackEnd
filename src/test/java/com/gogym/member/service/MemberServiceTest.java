package com.gogym.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gogym.gympay.entity.GymPay;
import com.gogym.member.dto.MemberProfileResponse;
import com.gogym.member.dto.UpdateMemberRequest;
import com.gogym.member.entity.Member;
import com.gogym.member.repository.BanNicknameRepository;
import com.gogym.member.repository.MemberRepository;
import com.gogym.member.type.MemberStatus;
import com.gogym.region.dto.RegionResponseDto;
import com.gogym.region.service.RegionService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class MemberServiceTest {

  @Mock
  private MemberRepository memberRepository;

  @Mock
  private BanNicknameRepository banNicknameRepository;

  @Mock
  private RegionService regionService;

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
    when(gymPay.getBalance()).thenReturn(10000);

    RegionResponseDto region1Dto = new RegionResponseDto("서울특별시", "강남구");
    RegionResponseDto region2Dto = new RegionResponseDto("서울특별시", "강북구");

    member = mock(Member.class);
    when(member.getId()).thenReturn(1L);
    when(member.getEmail()).thenReturn("test@example.com");
    when(member.getName()).thenReturn("John Doe");
    when(member.getNickname()).thenReturn("johndoe");
    when(member.getPhone()).thenReturn("01012345678");
    when(member.getProfileImageUrl()).thenReturn("profile.jpg");
    when(member.getRegionId1()).thenReturn(2L);
    when(member.getRegionId2()).thenReturn(3L);
    when(member.getGymPay()).thenReturn(gymPay);

    when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
    when(regionService.findById(member.getRegionId1())).thenReturn(region1Dto);
    when(regionService.findById(member.getRegionId2())).thenReturn(region2Dto);

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
    when(request.regionId1()).thenReturn(2L);
    when(request.regionId2()).thenReturn(3L);
    
    Member member = mock(Member.class);
    when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

    memberService.updateMyProfileById(memberId, request);

    verify(member).updateProfile(request.name(), request.nickname(), request.phone(),
        request.profileImageUrl(), request.regionId1(), request.regionId2());
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
