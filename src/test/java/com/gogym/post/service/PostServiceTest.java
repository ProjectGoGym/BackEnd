package com.gogym.post.service;

import static com.gogym.exception.ErrorCode.DELETED_POST;
import static com.gogym.exception.ErrorCode.FORBIDDEN;
import static com.gogym.exception.ErrorCode.POST_NOT_FOUND;
import static com.gogym.exception.ErrorCode.REQUEST_VALIDATION_FAIL;
import static com.gogym.post.type.MembershipType.MEMBERSHIP_ONLY;
import static com.gogym.post.type.PostStatus.COMPLETED;
import static com.gogym.post.type.PostStatus.HIDDEN;
import static com.gogym.post.type.PostStatus.IN_PROGRESS;
import static com.gogym.post.type.PostStatus.PENDING;
import static com.gogym.post.type.PostType.SELL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gogym.chat.entity.ChatRoom;
import com.gogym.chat.repository.ChatRoomRepository;
import com.gogym.chat.service.ChatRoomService;
import com.gogym.exception.CustomException;
import com.gogym.gympay.service.TransactionService;
import com.gogym.member.entity.Member;
import com.gogym.member.repository.MemberRepository;
import com.gogym.member.service.MemberService;
import com.gogym.post.dto.PostPageResponseDto;
import com.gogym.post.dto.PostRequestDto;
import com.gogym.post.dto.PostResponseDto;
import com.gogym.post.dto.PostUpdateRequestDto;
import com.gogym.post.entity.Gym;
import com.gogym.post.entity.Post;
import com.gogym.post.repository.GymRepository;
import com.gogym.post.repository.PostRepository;
import com.gogym.post.type.PostStatus;
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
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

  @Mock
  private PostRepository postRepository;

  @Mock
  private MemberRepository memberRepository;

  @Mock
  private GymService gymService;

  @Mock
  private GymRepository gymRepository;

  @Mock
  private RegionService regionService;

  @Mock
  private MemberService memberService;

  @Mock
  private RegionResponseDto regionResponseDto;

  @Mock
  private RecentViewService recentViewService;

  @Mock
  private ChatRoomService chatRoomService;

  @Mock
  private ChatRoomRepository chatRoomRepository;

  @Mock
  private TransactionService transactionService;
  
  @Mock
  private PostQueryService postQueryService;

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
  private PostUpdateRequestDto postUpdateRequestDto;
  private PostUpdateRequestDto postDeleteRequestDto;
  private ChatRoom chatRoom;
  private Member seller;
  private Member buyer;
  private PostStatus status;

  @BeforeEach
  void setUp() {

    member = Member.builder()
        .regionId1(1L)
        .regionId2(2L)
        .build();
    ReflectionTestUtils.setField(member, "id", 1L);

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
        .author(member)
        .gym(gym)
        .build();

    regionIds = List.of(1L, 2L);

    postUpdateRequestDto = new PostUpdateRequestDto("수정 제목", "내용", SELL, PENDING, MEMBERSHIP_ONLY,
        LocalDate.now().plusMonths(1), null, 1000L, null, null, null);

    postDeleteRequestDto = new PostUpdateRequestDto("제목", "내용", SELL, HIDDEN, MEMBERSHIP_ONLY,
        LocalDate.now().plusMonths(1), null, 1000L, null, null, null);
  }

  @Test
  void 헬스장이_존재하는_경우_성공적으로_게시글을_작성한다() {
    // given
    when(memberService.findById(member.getId())).thenReturn(member);
    when(gymService.findOrCreateGym(postRequestDto)).thenReturn(gym);
    when(regionService.findById(gym.getRegionId())).thenReturn(regionResponseDto);
    // when
    postService.createPost(member.getId(), postRequestDto);
    // then
    verify(postRepository).save(any());
  }

  @Test
  void 비회원이_게시글_목록을_조회한다() {
    // given
    posts = new PageImpl<>(List.of(post), pageable, 1);

    when(postRepository.findAllByStatusOrderByCreatedAtDesc(pageable, PENDING)).thenReturn(posts);
    // when
    Page<PostPageResponseDto> result = postService.getAllPosts(null, pageable);
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
    when(postRepository.findAllByStatusAndRegionIds(PENDING, pageable, regionIds)).thenReturn(
        posts);
    // when
    Page<PostPageResponseDto> result = postService.getAllPosts(member.getId(), pageable);
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
        .regionId1(null)
        .regionId2(null)
        .build();
    ReflectionTestUtils.setField(member, "id", 1L);

    when(memberService.findById(member.getId())).thenReturn(member);
    // when
    Page<PostPageResponseDto> result = postService.getAllPosts(member.getId(), pageable);
    // then
    assertNotNull(result);
    assertEquals(result.getTotalElements(), 0);
    assertTrue(result.getContent().isEmpty());
  }
  
  @Test
  void 게시글을_조회한다() {
    // given
    when(postQueryService.findById(post.getId())).thenReturn(post);
    when(regionService.findById(gym.getRegionId())).thenReturn(regionResponseDto);
    when(postQueryService.isWished(post, member.getId())).thenReturn(true);
    // when
    PostResponseDto result = postService.getDetailPost(member.getId(), post.getId());
    recentViewService.saveRecentView(member, post);
    // then
    assertNotNull(result);
    assertEquals(result.title(), post.getTitle());
    assertEquals(result.gymName(), post.getGym().getGymName());
  }

  @Test
  void 게시글이_없는_경우_조회에_실패한다() {
    // given
    when(postQueryService.findById(post.getId())).thenThrow(new CustomException(POST_NOT_FOUND));
    // when
    CustomException e = assertThrows(CustomException.class,
        () -> postService.getDetailPost(member.getId(), post.getId()));
    // then
    assertEquals(e.getErrorCode(), POST_NOT_FOUND);
    assertEquals(e.getMessage(), "게시글을 찾을 수 없습니다.");
  }

  @Test
  void 게시글_수정이_성공한다() {
    // given
    when(postQueryService.findById(post.getId())).thenReturn(post);
    when(memberService.findById(member.getId())).thenReturn(member);
    when(regionService.findById(gym.getRegionId())).thenReturn(regionResponseDto);
    when(postQueryService.isWished(post, member.getId())).thenReturn(false);
    // when
    postService.updatePost(member.getId(), post.getId(), postUpdateRequestDto);
    // then
    assertEquals(post.getTitle(), postUpdateRequestDto.title());
  }

  @Test
  void 게시글_작성자가_아니면_예외가_발생한다() {
    // given
    Member anotherMember = Member.builder().regionId1(10L).regionId2(11L).build();
    ReflectionTestUtils.setField(anotherMember, "id", 2L);
    post = Post.builder().title("게시글 제목").author(member).build();
    ReflectionTestUtils.setField(post, "id", 1L);
    when(postQueryService.findById(post.getId())).thenReturn(post);
    when(memberService.findById(anotherMember.getId())).thenReturn(anotherMember);
    // when
    CustomException e = assertThrows(CustomException.class,
        () -> postService.updatePost(anotherMember.getId(), post.getId(), postUpdateRequestDto));
    // then
    assertEquals(e.getErrorCode(), FORBIDDEN);
    assertEquals(e.getMessage(), "권한이 없습니다.");
    
  }

  @Test
  void 게시글을_삭제한후_게시글을_조회하면_예외가_발생한다() {
    // given
    when(postQueryService.findById(post.getId())).thenReturn(post);
    when(memberService.findById(member.getId())).thenReturn(member);
    when(regionService.findById(gym.getRegionId())).thenReturn(regionResponseDto);
    // when
    postService.updatePost(member.getId(), post.getId(), postDeleteRequestDto);
    CustomException e = assertThrows(CustomException.class,
        () -> postService.getDetailPost(member.getId(), post.getId()));
    // then
    assertEquals(post.getStatus(), HIDDEN);
    assertEquals(e.getErrorCode(), DELETED_POST);
    assertEquals(e.getMessage(), "삭제된 게시글입니다.");
  }

  @Test
  void 게시글의_상태를_성공적으로_변경한다() {
    // given
    seller = Member.builder().build();
    ReflectionTestUtils.setField(seller, "id", 1L);

    buyer = Member.builder().build();
    ReflectionTestUtils.setField(buyer, "id", 2L);

    chatRoom = ChatRoom.builder().post(post).requestor(buyer).build();
    ReflectionTestUtils.setField(chatRoom, "id", 1L);

    post = Post.builder().author(seller).status(PENDING).chatRoom(List.of(chatRoom)).build();
    ReflectionTestUtils.setField(post, "id", 1L);

    status = IN_PROGRESS;

    when(postQueryService.findById(post.getId())).thenReturn(post);
    when(memberService.findById(seller.getId())).thenReturn(seller);

    // when
    postService.changePostStatus(seller.getId(), post.getId(), chatRoom.getId(), status);

    // then
    assertEquals(post.getStatus(), IN_PROGRESS);
  }

  @Test
  void 게시글의_상태변경이_유효하지_않으면_예외가_발생한다() {
    // given
    seller = Member.builder().build();
    ReflectionTestUtils.setField(seller, "id", 1L);

    buyer = Member.builder().build();
    ReflectionTestUtils.setField(buyer, "id", 2L);

    chatRoom = ChatRoom.builder().post(post).requestor(buyer).build();
    ReflectionTestUtils.setField(chatRoom, "id", 1L);

    post = Post.builder().author(seller).status(COMPLETED).chatRoom(List.of(chatRoom)).build();
    status = IN_PROGRESS;

    when(postQueryService.findById(post.getId())).thenReturn(post);
    when(memberService.findById(seller.getId())).thenReturn(seller);
    // when
    CustomException e = assertThrows(CustomException.class,
        () -> postService.changePostStatus(seller.getId(), post.getId(), chatRoom.getId(), status));
    // then
    assertEquals(e.getErrorCode(), REQUEST_VALIDATION_FAIL);
    assertEquals(e.getMessage(), "'거래완료' 상태는 '거래중' 상태로 변경할 수 없습니다.");
  }

  @Test
  void 게시글의_상태가_HIDDEN_이면_다른상태로_변경할_수_없다() {
    // given
    seller = Member.builder().build();
    ReflectionTestUtils.setField(seller, "id", 1L);

    buyer = Member.builder().build();
    ReflectionTestUtils.setField(buyer, "id", 2L);
    chatRoom = ChatRoom.builder().post(post).requestor(buyer).build();

    ReflectionTestUtils.setField(chatRoom, "id", 1L);
    post = Post.builder().author(seller).status(HIDDEN).chatRoom(List.of(chatRoom)).build();
    status = IN_PROGRESS;

    when(memberService.findById(seller.getId())).thenReturn(seller);
    when(postQueryService.findById(post.getId())).thenReturn(post);
    // when
    CustomException e = assertThrows(CustomException.class,
        () -> postService.changePostStatus(seller.getId(), post.getId(), chatRoom.getId(), status));
    // then
    assertEquals(e.getErrorCode(), REQUEST_VALIDATION_FAIL);
    assertEquals(e.getMessage(), "'숨김처리' 상태는 '거래중' 상태로 변경할 수 없습니다.");
  }
}