package com.gogym.member.service;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import com.gogym.post.dto.PostResponseDto;
import com.gogym.post.entity.Post;
import com.gogym.post.repository.PostRepository;
import com.gogym.region.dto.RegionResponseDto;
import com.gogym.region.service.RegionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

class ActivityServiceTest {

  @Mock
  private PostRepository postRepository;

  @Mock
  private RegionService regionService;

  @InjectMocks
  private ActivityService activityService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testGetMyPosts() {
    Long memberId = 1L;
    Pageable pageable = Pageable.ofSize(5);
    Post post = mock(Post.class); // Post 객체를 Mock으로 생성
    Page<Post> postPage = new PageImpl<>(List.of(post), pageable, 1);

    when(postRepository.findByAuthorId(memberId, pageable)).thenReturn(postPage);
    when(regionService.findById(any())).thenReturn(new RegionResponseDto("Seoul", "Gangnam"));

    Page<PostResponseDto> result = activityService.getMyPosts(memberId, pageable);

    assertThat(result.getContent()).isNotEmpty();
    verify(postRepository).findByAuthorId(memberId, pageable);
  }

  @Test
  void testGetMyFavorites() {
    Long memberId = 1L;
    Pageable pageable = Pageable.ofSize(5);
    Post post = mock(Post.class); // Post 객체를 Mock으로 생성
    Page<Post> postPage = new PageImpl<>(List.of(post), pageable, 1);

    when(postRepository.findFavoritesByMemberId(memberId, pageable)).thenReturn(postPage);
    when(regionService.findById(any())).thenReturn(new RegionResponseDto("Seoul", "Gangnam"));

    Page<PostResponseDto> result = activityService.getMyFavorites(memberId, pageable);

    assertThat(result.getContent()).isNotEmpty();
    verify(postRepository).findFavoritesByMemberId(memberId, pageable);
  }

  @Test
  void testGetRecentViews() {
    Long memberId = 1L;
    Pageable pageable = Pageable.ofSize(5);
    Post post = mock(Post.class); // Post 객체를 Mock으로 생성
    Page<Post> postPage = new PageImpl<>(List.of(post), pageable, 1);

    when(postRepository.findRecentViewsByMemberId(memberId, pageable)).thenReturn(postPage);
    when(regionService.findById(any())).thenReturn(new RegionResponseDto("Seoul", "Gangnam"));

    Page<PostResponseDto> result = activityService.getRecentViews(memberId, pageable);

    assertThat(result.getContent()).isNotEmpty();
    verify(postRepository).findRecentViewsByMemberId(memberId, pageable);
  }
}

