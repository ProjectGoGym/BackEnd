package com.gogym.post.service;

import static com.gogym.exception.ErrorCode.REQUEST_VALIDATION_FAIL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gogym.exception.CustomException;
import com.gogym.member.entity.Member;
import com.gogym.member.service.MemberService;
import com.gogym.notification.service.NotificationService;
import com.gogym.post.dto.PostPageResponseDto;
import com.gogym.post.entity.Gym;
import com.gogym.post.entity.Post;
import com.gogym.post.entity.Wish;
import com.gogym.post.repository.WishRepository;
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

@ExtendWith(MockitoExtension.class)
class WishServiceTest {

  @Mock
  private MemberService memberService;

  @Mock
  private WishRepository wishRepository;

  @Mock
  private PostService postService;

  @Mock
  private NotificationService notificationService;

  @InjectMocks
  private WishService wishService;

  private Member member;
  private Wish wish;
  private Wish wish2;
  private Post post;
  private Post post2;
  private Pageable pageable;
  private Gym gym;

  @BeforeEach
  void setUp() {

    member = Member.builder().build();
    gym = Gym.builder().gymName("헬스장").build();
    post = Post.builder().author(member).gym(gym).wishCount(1L).build();
    post2 = Post.builder().author(member).gym(gym).wishCount(1L).build();
    wish = new Wish(member, post);
    wish2 = new Wish(member, post2);
    pageable = PageRequest.of(0, 10);

  }

  @Test
  void 찜이_존재하면_삭제하고_게시글의_찜_카운트가_0이된다() {
    // given
    when(memberService.findById(member.getId())).thenReturn(member);
    when(postService.findById(post.getId())).thenReturn(post);
    when(wishRepository.findByMemberAndPost(member, post)).thenReturn(Optional.of(wish));
    // when
    wishService.toggleWish(member.getId(), post.getId());
    // then
    verify(wishRepository).delete(wish);
    assertEquals(post.getWishCount(), 0);
  }

  @Test
  void 찜이_존재하지_않으면_추가하고_찜_카운트가_증가한다() {
    // given
    when(memberService.findById(member.getId())).thenReturn(member);
    when(postService.findById(post.getId())).thenReturn(post);
    when(wishRepository.findByMemberAndPost(member, post)).thenReturn(Optional.empty());
    // when
    wishService.toggleWish(member.getId(), post.getId());
    // then
    verify(wishRepository).save(any(Wish.class));
    assertEquals(post.getWishCount(), 2);
  }

  @Test
  void 찜을_삭제시_게시글의_찜_카운트가_0이면_예외가_발생한다() {
    // given
    Post post = Post.builder().wishCount(0L).build();
    when(memberService.findById(member.getId())).thenReturn(member);
    when(postService.findById(post.getId())).thenReturn(post);
    when(wishRepository.findByMemberAndPost(member, post)).thenReturn(Optional.of(wish));
    // when
    CustomException e = assertThrows(CustomException.class,
        () -> wishService.toggleWish(member.getId(), post.getId()));
    // then
    verify(wishRepository, never()).delete(wish);
    assertEquals(e.getErrorCode(), REQUEST_VALIDATION_FAIL);
    assertEquals(e.getMessage(), "잘못된 요청 값입니다.");
  }

  @Test
  void 회원의_찜이_존재하면_찜_목록을_반환한다() {
    // given
    List<Wish> wishList = List.of(wish, wish2);
    when(wishRepository.findByMemberIdOrderByCreatedAtDesc(member.getId(), pageable)).thenReturn(new PageImpl<>(
        wishList, pageable, wishList.size()));
    // when
    Page<PostPageResponseDto> wishPosts = wishService.getWishList(member.getId(), pageable);
    // then
    assertNotNull(wishPosts.getContent());
  }
}