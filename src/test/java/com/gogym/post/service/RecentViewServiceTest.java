package com.gogym.post.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gogym.common.paging.SortPage;
import com.gogym.member.entity.Member;
import com.gogym.post.dto.PostPageResponseDto;
import com.gogym.post.entity.Gym;
import com.gogym.post.entity.Post;
import com.gogym.post.entity.RecentView;
import com.gogym.post.repository.RecentViewRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class RecentViewServiceTest {

  @Mock
  private RecentViewRepository recentViewRepository;

  @Mock
  private SortPage sortPage;

  @InjectMocks
  private RecentViewService recentViewService;

  private static final int MAX_RECENT_VIEWS = 5;
  private Pageable pageable;
  private Member member;
  private Post post;
  private Gym gym;

  @BeforeEach
  void setUp() {

    pageable = PageRequest.of(0, 10);
    member = Member.builder().id(1L).build();
    gym = Gym.builder().gymName("헬스장").build();
    post = Post.builder().member(member).gym(gym).build();
  }

  @Test
  void 최근본_게시글이_4개면_저장한다() {
    // given
    when(recentViewRepository.existsByMemberAndPost(member, post)).thenReturn(false);
    when(recentViewRepository.countByMember(member)).thenReturn(4L);
    // when
    recentViewService.saveRecentView(member, post);
    // then
    verify(recentViewRepository).save(any(RecentView.class));
  }

  @Test
  void 최근본_게시글이_5개면_옛날_게시글을_삭제한다() {
    // given
    when(recentViewRepository.existsByMemberAndPost(member, post)).thenReturn(false);
    when(recentViewRepository.countByMember(member)).thenReturn(5L);
    // when
    recentViewService.saveRecentView(member, post);
    // then
    verify(recentViewRepository).deleteOldestByMember(member.getId());
    verify(recentViewRepository).save(any(RecentView.class));
  }

  @Test
  void 이미_본_게시글이면_저장하지_않는다() {
    // given
    when(recentViewRepository.existsByMemberAndPost(member, post)).thenReturn(true);
    // when
    recentViewService.saveRecentView(member, post);
    // then
    verify(recentViewRepository, never()).save(any(RecentView.class));
  }

  @Test
  void 최근본_게시글_목록을_반환한다() {
    // given
    when(sortPage.getSortPageable(pageable)).thenReturn(pageable);
    List<RecentView> recentViews = List.of(new RecentView(member, post));
    when(recentViewRepository.findByMemberId(member.getId(), pageable)).thenReturn(new PageImpl<>(recentViews, pageable, recentViews.size()));
    // when
    Page<PostPageResponseDto> recentViewPage = recentViewService.getRecentViews(member.getId(),
        pageable);
    // then
    assertNotNull(recentViewPage);
    assertEquals(recentViewPage.getTotalElements(), 1);
  }
}