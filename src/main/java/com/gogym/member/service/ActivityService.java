package com.gogym.member.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gogym.post.dto.PostResponseDto;
import com.gogym.post.repository.PostRepository;
import com.gogym.post.entity.Post;
import com.gogym.region.dto.RegionResponseDto;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActivityService {

  private final PostRepository postRepository;

  // 내가 작성한 게시글 조회
  public Page<PostResponseDto> getMyPosts(Long memberId, Pageable pageable) {
    return postRepository.findByAuthor_Id(memberId, pageable).map(post -> PostResponseDto
        .fromEntity(post, getRegionResponseDto(post.getGym().getRegionId())));
  }

  // 내가 찜한 게시글 조회
  public Page<PostResponseDto> getMyFavorites(Long memberId, Pageable pageable) {
    return postRepository.findFavoritesByAuthor_Id(memberId, pageable).map(post -> PostResponseDto
        .fromEntity(post, getRegionResponseDto(post.getGym().getRegionId())));
  }

  // 최근 본 게시글 조회
  public Page<PostResponseDto> getRecentViews(Long memberId, Pageable pageable) {
    return postRepository.findRecentViewsByAuthor_Id(memberId, pageable).map(post -> PostResponseDto
        .fromEntity(post, getRegionResponseDto(post.getGym().getRegionId())));
  }

  // RegionResponseDto를 반환하는 메서드
  private RegionResponseDto getRegionResponseDto(Long regionId) {
    String city = "City";
    String district = "District";
    return new RegionResponseDto(city, district);
  }
}


