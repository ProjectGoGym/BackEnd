package com.gogym.post.controller;

import com.gogym.post.dto.PostRequestDto;
import com.gogym.post.dto.PostResponseDto;
import com.gogym.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
class PostController {

  private final PostService postService;

  @PostMapping("/{member-id}")
  public ResponseEntity<PostResponseDto> createPost(@PathVariable("member-id") Long memberId,
      @RequestBody @Valid PostRequestDto postRequestDto) {

    PostResponseDto createdPost = postService.createPost(memberId, postRequestDto);

    return ResponseEntity.ok(createdPost);
  }
}