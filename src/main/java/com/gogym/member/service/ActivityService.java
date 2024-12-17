package com.gogym.member.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gogym.post.dto.PostResponseDto;
import com.gogym.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActivityService {

  private final PostRepository postRepository;

  // 내가 작성한 게시글 조회
  public Page<PostResponseDto> getMyPosts(Long memberId, Pageable pageable) {
    return postRepository.findByMember_Id(memberId, pageable).map(PostResponseDto::fromEntity);
  }

  // 내가 찜한 게시글 조회
  public Page<PostResponseDto> getMyFavorites(Long memberId, Pageable pageable) {
    return postRepository.findFavoritesByMemberId(memberId, pageable)
        .map(PostResponseDto::fromEntity);
  }

  // 최근 본 게시글 조회
  public Page<PostResponseDto> getRecentViews(Long memberId, Pageable pageable) {
    return postRepository.findRecentViewsByMemberId(memberId, pageable)
        .map(PostResponseDto::fromEntity);
  }
}

