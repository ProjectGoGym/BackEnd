package com.gogym.post.service;

import static com.gogym.exception.ErrorCode.POST_NOT_FOUND;
import static com.gogym.post.type.MembershipType.MEMBERSHIP_ONLY;
import static com.gogym.post.type.PostStatus.POSTING;
import static com.gogym.post.type.PostType.SELL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gogym.exception.CustomException;
import com.gogym.member.entity.Member;
import com.gogym.member.repository.MemberRepository;
import com.gogym.member.service.MemberService;
import com.gogym.post.dto.PostPageResponseDto;
import com.gogym.post.dto.PostRequestDto;
import com.gogym.post.dto.PostResponseDto;
import com.gogym.post.entity.Gym;
import com.gogym.post.entity.Post;
import com.gogym.post.repository.GymRepository;
import com.gogym.post.repository.PostRepository;
import com.gogym.region.dto.RegionResponseDto;
import com.gogym.region.entity.Region;
import com.gogym.region.service.RegionService;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

  @Mock
  private PostRepository postRepository;

  @Mock
  private MemberRepository memberRepository;

  @Mock
  private GymRepository gymRepository;

  @Mock
  private RegionService regionService;

  @Mock
  private MemberService memberService;

  @Mock
  private RegionResponseDto regionResponseDto;

  @InjectMocks
  private PostService postService;

  private Member member;
  private Gym gym;
  private Region parent;
  private Region child;
  private PostRequestDto postRequestDto;
  private Pageable pageable;
  private Post post;
  private List<Long> regionIds;
  private Page<Post> posts;

  @BeforeEach
  void setUp() {

    member = Member.builder()
        .id(1L)
        .regionId1(1L)
        .regionId2(2L)
        .build();
    parent = Region.builder()
        .id(1L)
        .name("도시")
        .build();
    child = Region.builder()
        .id(2L)
        .name("지역")
        .build();
    gym = Gym.builder()
        .gymName("테스트 헬스장")
        .latitude(1.1)
        .longitude(2.2)
        .regionId(parent.getId())
        .build();
    postRequestDto = new PostRequestDto("제목", "내용", SELL, MEMBERSHIP_ONLY,
        LocalDate.now().plusMonths(10), null, 1000L,
        "url1", "url2", "url3",
        "테스트 헬스장", 1.1, 2.2,
        "url", "도시", "지역");

    pageable = PageRequest.of(0, 10, Sort.by(Direction.DESC, "createdAt"));

    post = Post.builder()
        .title("게시글 제목")
        .member(member)
        .gym(gym)
        .build();

    regionIds = List.of(1L, 2L);
  }

  @Test
  void 헬스장이_존재하는_경우_성공적으로_게시글을_작성한다() {
    // given
    when(memberService.findById(member.getId())).thenReturn(member);
    when(gymRepository.findByLatitudeAndLongitude(postRequestDto.latitude(),
        postRequestDto.longitude())).thenReturn(Optional.of(gym));
    when(regionService.findById(gym.getRegionId())).thenReturn(regionResponseDto);
    // when
    postService.createPost(member.getId(), postRequestDto);
    // then
    verify(postRepository).save(any());
  }

  @Test
  void 헬스장이_존재하지_않는_경우_헬스장을_저장한다() {
    // given
    when(memberService.findById(member.getId())).thenReturn(member);
    when(gymRepository.findByLatitudeAndLongitude(postRequestDto.latitude(),
        postRequestDto.longitude())).thenReturn(Optional.empty());
    when(gymRepository.save(any(Gym.class))).thenReturn(gym);
    when(regionService.findById(gym.getRegionId())).thenReturn(regionResponseDto);
    // when
    postService.createPost(member.getId(), postRequestDto);
    // then
    verify(gymRepository).save(any(Gym.class));
    verify(postRepository).save(any());
  }

  @Test
  void 비회원이_게시글_목록을_조회한다() {
    // given
    posts = new PageImpl<>(List.of(post), pageable, 1);

    when(postRepository.findAllByStatus(pageable, POSTING)).thenReturn(posts);
    // when
    Page<PostPageResponseDto> result = postService.getAllPostsOfGuest(pageable);
    // then
    assertNotNull(result);
    assertEquals(result.getTotalElements(), 1);
    assertEquals(result.getContent().get(0).title(), "게시글 제목");
  }

  @Test
  void 회원이_게시글_목록을_조회한다() {
    // given
    posts = new PageImpl<>(List.of(post), pageable, 1);

    when(memberService.findById(member.getId())).thenReturn(member);
    when(postRepository.findAllByStatusAndRegionIds(POSTING, pageable, regionIds)).thenReturn(
        posts);
    // when
    Page<PostPageResponseDto> result = postService.getAllPostsOfMember(member.getId(), pageable);
    // then
    assertNotNull(result);
    assertEquals(result.getTotalElements(), 1);
    assertEquals(result.getContent().get(0).title(), "게시글 제목");
  }

  @Test
  void 회원의_지역이_설정이_되지_않고_해당_지역에_등록된_게시글이_없으면_빈_배열을_반환한다() {
    // given
    posts = new PageImpl<>(List.of(post), pageable, 1);
    member = Member.builder()
        .id(1L)
        .regionId1(null)
        .regionId2(null)
        .build();

    when(memberService.findById(member.getId())).thenReturn(member);
    // when
    Page<PostPageResponseDto> result = postService.getAllPostsOfMember(member.getId(), pageable);
    // then
    assertNotNull(result);
    assertEquals(result.getTotalElements(), 0);
    assertTrue(result.getContent().isEmpty());
  }

  @Test
  void 게시글을_조회한다() {
    // given
    when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
    when(regionService.findById(gym.getRegionId())).thenReturn(regionResponseDto);
    // when
    PostResponseDto result = postService.getDetailPost(post.getId());
    // then
    assertNotNull(result);
    assertEquals(result.title(), post.getTitle());
    assertEquals(result.gymName(), post.getGym().getGymName());
  }

  @Test
  void 게시글이_없는_경우_조회에_실패한다() {
    // given
    when(postRepository.findById(post.getId())).thenReturn(Optional.empty());
    // when
    CustomException e = assertThrows(CustomException.class,
        () -> postService.getDetailPost(post.getId()));
    // then
    assertEquals(e.getErrorCode(), POST_NOT_FOUND);
    assertEquals(e.getMessage(), "게시글을 찾을 수 없습니다.");
  }
}