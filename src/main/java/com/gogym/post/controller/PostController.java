package com.gogym.post.controller;

import com.gogym.common.annotation.LoginMemberId;
import com.gogym.post.dto.PostFilterRequestDto;
import com.gogym.post.dto.PostPageResponseDto;
import com.gogym.post.dto.PostRequestDto;
import com.gogym.post.dto.PostResponseDto;
import com.gogym.post.dto.PostUpdateRequestDto;
import com.gogym.post.service.PostService;
import com.gogym.post.service.WishService;
import com.gogym.post.type.FilterMonthsType;
import com.gogym.post.type.FilterPtType;
import com.gogym.post.type.MembershipType;
import com.gogym.post.type.PostStatus;
import com.gogym.post.type.PostType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

  private final PostService postService;

  private final WishService wishService;

  @PostMapping
  public ResponseEntity<PostResponseDto> createPost(@LoginMemberId Long memberId,
      @RequestBody @Valid PostRequestDto postRequestDto) {

    PostResponseDto createdPost = postService.createPost(memberId, postRequestDto);

    return ResponseEntity.ok(createdPost);
  }

  // 토큰이 존재하면 회원, 존재하지 않으면 비회원의 게시글 목록 조회 입니다.
  @GetMapping("/views")
  public ResponseEntity<Page<PostPageResponseDto>> getAllPostsOfMember(
      @LoginMemberId(required = false) Long memberId, Pageable pageable) {
    Page<PostPageResponseDto> page = postService.getAllPosts(memberId, pageable);

    return ResponseEntity.ok(page);
  }

  // 토큰이 존재하면 회원, 존재하지 않으면 비회원의 필터링 된 게시글 목록 조회 입니다. 쿼리 파라미터로 값을 받습니다.
  @GetMapping("/filters")
  public ResponseEntity<Page<PostPageResponseDto>> getFilterPostsOfMember(
      @LoginMemberId(required = false) Long memberId,
      @RequestParam(value = "post-type", required = false) PostType postType,
      @RequestParam(value = "membership-type", required = false) MembershipType membershipType,
      @RequestParam(value = "status", required = false) PostStatus status,
      @RequestParam(value = "months-type", required = false) FilterMonthsType monthsType,
      @RequestParam(value = "pt-type", required = false) FilterPtType ptType
      , Pageable pageable) {

    PostFilterRequestDto postFilterRequestDto = new PostFilterRequestDto(postType, membershipType,
        status, monthsType, ptType);

    Page<PostPageResponseDto> page = postService.getFilterPosts(memberId,
        postFilterRequestDto,
        pageable);

    return ResponseEntity.ok(page);
  }

  // 게시글 상세 조회 입니다.
  @GetMapping("/details/{post-id}")
  public ResponseEntity<PostResponseDto> getDetailPost(
      @LoginMemberId(required = false) Long memberId,
      @PathVariable("post-id") Long postId) {

    PostResponseDto postResponseDto = postService.getDetailPost(memberId, postId);

    return ResponseEntity.ok(postResponseDto);
  }

  // 게시글 수정과 삭제를 처리(Soft Delete) 합니다.
  @PutMapping("/{post-id}")
  public ResponseEntity<PostResponseDto> updatePost(@LoginMemberId Long memberId,
      @PathVariable("post-id") Long postId,
      @Valid @RequestBody PostUpdateRequestDto postUpdateRequestDto
  ) {

    PostResponseDto postResponseDto = postService.updatePost(memberId, postId,
        postUpdateRequestDto);

    return ResponseEntity.ok(postResponseDto);
  }

  // 게시글을 찜 추가, 삭제 합니다.
  @PostMapping("/{post-id}/wishes")
  public ResponseEntity<Void> toggleWish(@LoginMemberId Long memberId,
      @PathVariable("post-id") Long postId) {

    wishService.toggleWish(memberId, postId);

    return ResponseEntity.ok().build();
  }

  // 게시글의 상태를 변경합니다.
  @PutMapping("/{post-id}/change")
  public ResponseEntity<Void> changePostStatus(
      @LoginMemberId Long memberId,
      @PathVariable("post-id") Long postId,
      @RequestParam("chat-room-id") Long chatRoomId,
      @RequestParam("status") PostStatus status) {

    postService.changePostStatus(memberId, postId, chatRoomId, status);

    return ResponseEntity.ok().build();
  }
}