/*
package com.gogym.member.service;

import com.gogym.exception.CustomException;
import com.gogym.member.dto.MemberProfileResponse;
import com.gogym.member.dto.UpdateMemberRequest;
import com.gogym.member.entity.Member;
import com.gogym.member.repository.BanNicknameRepository;
import com.gogym.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

class MemberServiceTest {

  @InjectMocks
  private MemberService memberService;

  @Mock
  private MemberRepository memberRepository;

  private Member mockMember;

  private BanNicknameRepository banNicknameRepository;

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

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    mockMember = Member.builder().id(1L).email("test@example.com").name("Tester")
        .nickname("Test Nickname").phone("010-1234-5678").profileImageUrl("profile.jpg").build();
  }

  @Test
  void 내_정보를_조회한다() {
    // given
    when(memberRepository.findById(anyLong())).thenReturn(Optional.of(mockMember));

    // when
    MemberProfileResponse response = memberService.getMyProfileById(1L);

    // then
    assertThat(response.email()).isEqualTo("test@example.com");
    assertThat(response.name()).isEqualTo("Tester");
    assertThat(response.nickname()).isEqualTo("Test Nickname");
    assertThat(response.phone()).isEqualTo("010-1234-5678");
  }

  @Test
  void 내_정보를_수정한다() {
    // given
    UpdateMemberRequest request =
        new UpdateMemberRequest("New Name", "New Nickname", "010-9876-5432", "new-profile.jpg", 1L, // regionId1
            2L // regionId2
        );

    when(memberRepository.findById(anyLong())).thenReturn(Optional.of(mockMember));

    // when
    memberService.updateMyProfileById(1L, request);

    // then
    assertThat(mockMember.getName()).isEqualTo("New Name");
    assertThat(mockMember.getNickname()).isEqualTo("New Nickname");
    assertThat(mockMember.getPhone()).isEqualTo("010-9876-5432");
  }

  @Test
  void 회원_탈퇴를_소프트로_처리한다() {
    // given
    when(memberRepository.findById(anyLong())).thenReturn(Optional.of(mockMember));

    // 예상 마스킹 결과
    String expectedMaskedName = maskString(mockMember.getName());
    String expectedMaskedNickname = maskString(mockMember.getNickname());
    String expectedMaskedEmail = maskEmail(mockMember.getEmail());

    // when
    memberService.deactivateMyAccountById(1L);

    // then
    assertThat(mockMember.getName()).isEqualTo(expectedMaskedName);
    assertThat(mockMember.getNickname()).isEqualTo(expectedMaskedNickname);
    assertThat(mockMember.getEmail()).isEqualTo(expectedMaskedEmail);
    assertThat(mockMember.getPhone()).isNull();
    assertThat(mockMember.getProfileImageUrl()).isNull();
  }
}
*/
