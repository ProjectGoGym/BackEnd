package com.gogym.post.controller;

import com.gogym.common.annotation.LoginMemberId;
import com.gogym.post.dto.PostFilterRequestDto;
import com.gogym.post.dto.PostPageResponseDto;
import com.gogym.post.dto.PostRequestDto;
import com.gogym.post.dto.PostResponseDto;
import com.gogym.post.service.PostService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
class PostController {

  private final PostService postService;

  @PostMapping
  public ResponseEntity<PostResponseDto> createPost(@LoginMemberId Long memberId,
      @RequestBody @Valid PostRequestDto postRequestDto) {

    PostResponseDto createdPost = postService.createPost(memberId, postRequestDto);

    return ResponseEntity.ok(createdPost);
  }

  // 비회원의 게시글 목록 조회 입니다.
  @GetMapping
  public ResponseEntity<Page<PostPageResponseDto>> getAllPosts(Pageable pageable) {

    Page<PostPageResponseDto> page = postService.getAllPosts(null, pageable);

    return ResponseEntity.ok(page);
  }

  // 회원의 게시글 목록 조회 입니다.
  @GetMapping("/members")
  public ResponseEntity<Page<PostPageResponseDto>> getAllPostsOfMember(
      @LoginMemberId Long memberId, Pageable pageable) {

    Page<PostPageResponseDto> page = postService.getAllPosts(memberId, pageable);

    return ResponseEntity.ok(page);
  }

  // 비회원의 필터링 된 게시글 목록 조회 입니다. 쿼리 파라미터로 값을 받습니다.
  @GetMapping("/guests/filters")
  public ResponseEntity<Page<PostPageResponseDto>> getFilterPostsOfGuest(
      @RequestParam(value = "post-type", required = false) PostType postType,
      @RequestParam(value = "membership-type", required = false) MembershipType membershipType,
      @RequestParam(value = "status", required = false) PostStatus status,
      @RequestParam(value = "months-type", required = false) FilterMonthsType monthsType,
      @RequestParam(value = "pt-type", required = false) FilterPtType ptType
      , Pageable pageable) {

    PostFilterRequestDto postFilterRequestDto = new PostFilterRequestDto(postType, membershipType,
        status, monthsType, ptType);

    Page<PostPageResponseDto> page = postService.getFilterPosts(null, postFilterRequestDto,
        pageable);

    return ResponseEntity.ok(page);
  }

  // 회원의 필터링 된 게시글 목록 조회 입니다. 쿼리 파라미터로 값을 받습니다.
  @GetMapping("/members/filters")
  public ResponseEntity<Page<PostPageResponseDto>> getFilterPostsOfMember(
      @LoginMemberId Long memberId,
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
  public ResponseEntity<PostResponseDto> getDetailPost(@PathVariable("post-id") Long postId) {

    PostResponseDto postResponseDto = postService.getDetailPost(postId);

    return ResponseEntity.ok(postResponseDto);
  }
}