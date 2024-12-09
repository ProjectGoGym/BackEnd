package com.gogym.member.service;

import com.gogym.exception.CustomException;
import com.gogym.exception.ErrorCode;
import com.gogym.member.dto.MemberProfileResponse;
import com.gogym.member.dto.UpdateMemberRequest;
import com.gogym.member.entity.Member;
import com.gogym.member.repository.MemberRepository;
import com.gogym.post.dto.PostResponseDto;
import com.gogym.post.entity.Post;
import com.gogym.post.repository.FavoriteRepository;
import com.gogym.post.repository.PostRepository;
import com.gogym.post.repository.ViewHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.gogym.post.entity.Gym;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

class MemberServiceTest {

  @InjectMocks
  private MemberService memberService;

  @Mock
  private MemberRepository memberRepository;

  @Mock
  private PostRepository postRepository;

  @Mock
  private FavoriteRepository favoriteRepository;

  @Mock
  private ViewHistoryRepository viewHistoryRepository;

  private Member mockMember;
  private Post mockPost;
  private Pageable pageable;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    Gym mockGym = Gym.builder().gymName("Test Gym").latitude(37.0000).longitude(125.0000)
        .gymKakaoUrl("http://test-gym.com").regionId(1L).build();

    mockMember = Member.builder().id(1L).email("test@example.com").name("Tester")
        .nickname("Test Nickname").phone("010-1234-5678").profileImageUrl("profile.jpg").build();

    mockPost = Post.builder().title("게시글 제목").content("게시글 내용").amount(1000L).gym(mockGym)
        .member(mockMember).build();

    pageable = PageRequest.of(0, 10);
  }

  @Test
  void 내_정보를_조회한다() {
    // given
    when(memberRepository.findById(anyLong())).thenReturn(Optional.of(mockMember));

    // when
    MemberProfileResponse response = memberService.getMyProfileById(1L);

    // then
    assertThat(response.email()).isEqualTo("test@example.com");
    assertThat(response.name()).isEqualTo("Tester");
    assertThat(response.nickname()).isEqualTo("Test Nickname");
    assertThat(response.phone()).isEqualTo("010-1234-5678");
  }

  @Test
  void 내_정보를_수정한다() {
    // given
    UpdateMemberRequest request =
        new UpdateMemberRequest("New Name", "New Nickname", "010-9876-5432", "new-profile.jpg");

    when(memberRepository.findById(anyLong())).thenReturn(Optional.of(mockMember));

    // when
    memberService.updateMyProfileById(1L, request);

    // then
    assertThat(mockMember.getName()).isEqualTo("New Name");
    assertThat(mockMember.getNickname()).isEqualTo("New Nickname");
    assertThat(mockMember.getPhone()).isEqualTo("010-9876-5432");
  }

  @Test
  void 회원_탈퇴를_처리한다() {
    // given
    when(memberRepository.findById(anyLong())).thenReturn(Optional.of(mockMember));

    // when
    memberService.deleteMyAccountById(1L);

    // then
    assertThat(mockMember).isNotNull();
  }

  @Test
  void 내가_작성한_게시글을_조회한다() {
    // given
    Page<Post> mockPosts = new PageImpl<>(List.of(mockPost), pageable, 1);
    when(postRepository.findByMember_Id(anyLong(), any(Pageable.class))).thenReturn(mockPosts);

    // when
    Page<PostResponseDto> result = memberService.getMyPostsById(1L, 0, 10);

    // then
    assertThat(result.getContent().get(0).title()).isEqualTo("게시글 제목");
  }

  @Test
  void 내가_찜한_게시글을_조회한다() {
    // given
    Page<Post> mockFavorites = new PageImpl<>(List.of(mockPost), pageable, 1);
    when(favoriteRepository.findFavoritesByMemberId(anyLong(), any(Pageable.class)))
        .thenReturn(mockFavorites);

    // when
    Page<PostResponseDto> result = memberService.getMyFavoritesById(1L, 0, 10);

    // then
    assertThat(result.getContent().get(0).title()).isEqualTo("게시글 제목");
  }

  @Test
  void 최근_본_게시글을_조회() {
    // given
    Page<Post> mockViews = new PageImpl<>(List.of(mockPost), pageable, 1);
    when(viewHistoryRepository.findRecentViewsByMemberId(anyLong(), any(Pageable.class)))
        .thenReturn(mockViews);

    // when
    Page<PostResponseDto> result = memberService.getRecentViewsById(1L, 0, 10);

    // then
    assertThat(result.getContent().get(0).title()).isEqualTo("게시글 제목");
  }
}
