package com.gogym.member.service;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import com.gogym.member.entity.Member;
import com.gogym.member.type.MemberStatus;
import com.gogym.post.dto.PostPageResponseDto;
import com.gogym.post.service.PostService;
import com.gogym.post.service.WishService;
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
  private PostService postService;

  @Mock
  private WishService wishService;

  @Mock
  private MemberService memberService;

  @InjectMocks
  private ActivityService activityService;

  private Pageable pageable;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    this.pageable = Pageable.ofSize(5);
  }

  @Test
  void 내가_작성한_게시글_조회_성공() {
    // Given
    Long memberId = 1L;
    PostPageResponseDto responseDto = mock(PostPageResponseDto.class);
    Page<PostPageResponseDto> postPage = new PageImpl<>(List.of(responseDto), pageable, 1);

    Member mockMember = mock(Member.class);
    when(mockMember.getMemberStatus()).thenReturn(MemberStatus.ACTIVE);
    when(memberService.findById(memberId)).thenReturn(mockMember);

    when(postService.getAuthorPosts(memberId, pageable)).thenReturn(postPage);

    // When
    Page<PostPageResponseDto> result = activityService.getMyPosts(memberId, pageable);

    // Then
    assertThat(result.getContent()).isNotEmpty();
    verify(postService).getAuthorPosts(memberId, pageable);
  }

  @Test
  void 내가_찜한_게시글_조회_성공() {
    // Given
    Long memberId = 1L;
    PostPageResponseDto responseDto = mock(PostPageResponseDto.class);
    Page<PostPageResponseDto> wishPage = new PageImpl<>(List.of(responseDto), pageable, 1);

    Member mockMember = mock(Member.class);
    when(mockMember.getMemberStatus()).thenReturn(MemberStatus.ACTIVE);
    when(memberService.findById(memberId)).thenReturn(mockMember);

    when(wishService.getWishList(memberId, pageable)).thenReturn(wishPage);

    // When
    Page<PostPageResponseDto> result = activityService.getMyFavorites(memberId, pageable);

    // Then
    assertThat(result.getContent()).isNotEmpty();
    verify(wishService).getWishList(memberId, pageable);
  }

  @Test
  void 최근_본_게시글_조회_성공() {
    // Given
    Long memberId = 1L;
    PostPageResponseDto responseDto = mock(PostPageResponseDto.class);
    Page<PostPageResponseDto> recentViewPage = new PageImpl<>(List.of(responseDto), pageable, 1);

    Member mockMember = mock(Member.class);
    when(mockMember.getMemberStatus()).thenReturn(MemberStatus.ACTIVE);
    when(memberService.findById(memberId)).thenReturn(mockMember);

    when(postService.getAuthorPosts(memberId, pageable)).thenReturn(recentViewPage);

    // When
    Page<PostPageResponseDto> result = activityService.getRecentViews(memberId, pageable);

    // Then
    assertThat(result.getContent()).isNotEmpty();
    verify(postService).getAuthorPosts(memberId, pageable);
  }
}


