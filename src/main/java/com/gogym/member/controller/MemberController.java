package com.gogym.member.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.gogym.common.annotation.LoginMemberId;
import com.gogym.member.dto.MemberProfileResponse;
import com.gogym.member.dto.UpdateMemberRequest;
import com.gogym.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

  private final MemberService memberService;

  // 내 정보 조회
  @GetMapping("/me/profile")
  public ResponseEntity<MemberProfileResponse> getMyProfile(@LoginMemberId Long memberId) {
    return ResponseEntity.ok(memberService.getMyProfileById(memberId));
  }

  // 내 정보 수정
  @PutMapping("/me/profile")
  public ResponseEntity<Void> updateMyProfile(@LoginMemberId Long memberId,
      @RequestBody @Valid UpdateMemberRequest request) {
    memberService.updateMyProfileById(memberId, request);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/me")
  public ResponseEntity<Void> deactivateMyAccount(@LoginMemberId Long memberId) {
    memberService.deleteMyAccountById(memberId);
    return ResponseEntity.ok().build();
  }
}
