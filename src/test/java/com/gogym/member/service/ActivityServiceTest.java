package com.gogym.member.service;

import com.gogym.post.dto.PostResponseDto;
import com.gogym.post.entity.Post;
import com.gogym.post.entity.Gym;
import com.gogym.post.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

class ActivityServiceTest {

  @InjectMocks
  private ActivityService activityService;

  @Mock
  private PostRepository postRepository;

  private Post mockPost;
  private Pageable pageable;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    Gym mockGym = Gym.builder().gymName("Test Gym").latitude(37.0000).longitude(125.0000)
        .gymKakaoUrl("http://test-gym.com").regionId(1L).build();

    mockPost = Post.builder().title("게시글 제목").content("게시글 내용").amount(1000L).gym(mockGym).build();

    pageable = PageRequest.of(0, 10);
  }

  @Test
  void 내가_작성한_게시글을_조회한다() {
    // given
    Page<Post> mockPosts = new PageImpl<>(List.of(mockPost), pageable, 1);
    when(postRepository.findByAuthor_Id(anyLong(), any(Pageable.class))).thenReturn(mockPosts);

    // when
    Page<PostResponseDto> result = activityService.getMyPosts(1L, pageable);

    // then
    assertThat(result.getContent().get(0).title()).isEqualTo("게시글 제목");
  }

  @Test
  void 내가_찜한_게시글을_조회한다() {
    // given
    Page<Post> mockFavorites = new PageImpl<>(List.of(mockPost), pageable, 1);
    when(postRepository.findFavoritesByAuthor_Id(anyLong(), any(Pageable.class)))
        .thenReturn(mockFavorites);

    // when
    Page<PostResponseDto> result = activityService.getMyFavorites(1L, pageable);

    // then
    assertThat(result.getContent().get(0).title()).isEqualTo("게시글 제목");
  }

  @Test
  void 최근_본_게시글을_조회() {
    // given
    Page<Post> mockViews = new PageImpl<>(List.of(mockPost), pageable, 1);
    when(postRepository.findRecentViewsByAuthor_Id(anyLong(), any(Pageable.class)))
        .thenReturn(mockViews);

    // when
    Page<PostResponseDto> result = activityService.getRecentViews(1L, pageable);

    // then
    assertThat(result.getContent().get(0).title()).isEqualTo("게시글 제목");
  }
}
