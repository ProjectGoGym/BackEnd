package com.gogym.member.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.gogym.common.annotation.LoginMemberId;
import com.gogym.member.dto.MemberProfileResponse;
import com.gogym.member.dto.UpdateMemberRequest;
import com.gogym.member.service.MemberService;
import com.gogym.post.dto.PostResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

  private final MemberService memberService;

  // 내 정보 조회
  @GetMapping("/me")
  public ResponseEntity<MemberProfileResponse> getMyProfile(@LoginMemberId Long memberId) {
    return ResponseEntity.ok(memberService.getMyProfileById(memberId));
  }

  // 내 정보 수정
  @PutMapping("/me")
  public ResponseEntity<Void> updateMyProfile(@LoginMemberId Long memberId,
      @RequestBody @Valid UpdateMemberRequest request) {
    memberService.updateMyProfileById(memberId, request);
    return ResponseEntity.ok().build();
  }

  // 회원 탈퇴
  @DeleteMapping("/withdrow")
  public ResponseEntity<Void> deleteMyAccount(@LoginMemberId Long memberId) {
    memberService.deleteMyAccountById(memberId);
    return ResponseEntity.ok().build();
  }

  // 내가 작성한 게시글
  @GetMapping("/my-posts")
  public ResponseEntity<Page<PostResponseDto>> getMyPosts(@LoginMemberId Long memberId,
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
    return ResponseEntity.ok(memberService.getMyPostsById(memberId, page, size));
  }

  // 내가 찜한 게시글
  @GetMapping("/wishlist")
  public ResponseEntity<Page<PostResponseDto>> getMyFavorites(@LoginMemberId Long memberId,
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
    return ResponseEntity.ok(memberService.getMyFavoritesById(memberId, page, size));
  }

  // 최근 본 게시글
  @GetMapping("/recent-view")
  public ResponseEntity<Page<PostResponseDto>> getRecentViews(@LoginMemberId Long memberId,
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
    return ResponseEntity.ok(memberService.getRecentViewsById(memberId, page, size));
  }
}
