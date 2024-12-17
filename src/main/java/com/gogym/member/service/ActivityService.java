package com.gogym.member.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gogym.post.dto.PostResponseDto;
import com.gogym.post.repository.PostRepository;
import com.gogym.region.dto.RegionResponseDto;
import com.gogym.region.service.RegionService;
import lombok.RequiredArgsConstructor;
import com.querydsl.core.BooleanBuilder;
import com.gogym.post.entity.QPost;
import com.gogym.post.entity.QRecentView;
import com.gogym.post.entity.QWish;
import com.gogym.post.dto.PostFilterRequestDto;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActivityService {

  private final PostRepository postRepository;
  private final RegionService regionService;

  // 내가 작성한 게시글 조회
  public Page<PostResponseDto> getMyPosts(Long memberId, Pageable pageable) {
    return postRepository.findByAuthorId(memberId, pageable).map(post -> PostResponseDto
        .fromEntity(post, regionService.findById(post.getGym().getRegionId())));
  }

  // 내가 찜한 게시글 조회
  public Page<PostResponseDto> getMyFavorites(Long memberId, Pageable pageable) {
    return postRepository.findFavoritesByMemberId(memberId, pageable).map(post -> PostResponseDto
        .fromEntity(post, regionService.findById(post.getGym().getRegionId())));
  }

  public Page<PostResponseDto> getRecentViews(Long memberId, Pageable pageable) {
    return postRepository.findRecentViewsByMemberId(memberId, pageable).map(post -> PostResponseDto
        .fromEntity(post, regionService.findById(post.getGym().getRegionId())));
  }
}

