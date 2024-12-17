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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActivityService {

  private final PostRepository postRepository;
  private final RegionService regionService;

  // 내가 작성한 게시글 조회
  public Page<PostResponseDto> getMyPosts(Long memberId, Pageable pageable) {
    return postRepository.findAllWithFilter(null, null, pageable).map(post -> {
      RegionResponseDto region = regionService.findById(post.getGym().getRegionId());
      return PostResponseDto.fromEntity(post, region);
    });
  }

  // 내가 찜한 게시글 조회
  public Page<PostResponseDto> getMyFavorites(Long memberId, Pageable pageable) {
    return postRepository.findAllWithFilter(null, null, pageable).map(post -> {
      RegionResponseDto region = regionService.findById(post.getGym().getRegionId());
      return PostResponseDto.fromEntity(post, region);
    });
  }

  // 최근 본 게시글 조회
  public Page<PostResponseDto> getRecentViews(Long memberId, Pageable pageable) {
    return postRepository.findAllWithFilter(null, null, pageable).map(post -> {
      RegionResponseDto region = regionService.findById(post.getGym().getRegionId());
      return PostResponseDto.fromEntity(post, region);
    });
  }
}

