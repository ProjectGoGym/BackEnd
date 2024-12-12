package com.gogym.post.service;

import static com.gogym.notification.type.NotificationType.ADD_WISHLIST_MY_POST;

import com.gogym.common.paging.SortPage;
import com.gogym.member.entity.Member;
import com.gogym.member.service.MemberService;
import com.gogym.notification.dto.NotificationDto;
import com.gogym.notification.service.NotificationService;
import com.gogym.post.dto.PostPageResponseDto;
import com.gogym.post.entity.Post;
import com.gogym.post.entity.Wish;
import com.gogym.post.repository.WishRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WishService {

  private final MemberService memberService;

  private final PostService postService;

  private final WishRepository wishRepository;

  private final NotificationService notificationService;

  private final SortPage sortPage;

  // 회원이 게시글을 찜 합니다.
  @Transactional
  public void toggleWish(Long memberId, Long postId) {

    Member member = memberService.findById(memberId);

    Post post = postService.findById(postId);

    Wish existingWish = wishRepository.findByMemberAndPost(member, post).orElse(null);

    // 찜이 존재하면 찜을 삭제합니다.
    if (existingWish != null) {
      removeWish(existingWish, post);
    } else {
      // 찜이 존해하지않으면 찜을 추가합니다.
      addWish(member, post);
    }
  }

  private void addWish(Member member, Post post) {

    Wish wish = new Wish(member, post);
    wishRepository.save(wish);
    // 게시글의 찜 횟수를 올립니다.
    post.increaseWishCount();
    // 게시글의 작성자에게 알림을 보냅니다.
    sendNotification(member, post);
  }

  private void removeWish(Wish existingWish, Post post) {

    // 게시글의 찜 횟수를 줄입니다.
    post.decreaseWishCount();
    wishRepository.delete(existingWish);
  }

  // 게시글의 작성자에게 알림을 보내는 메서드입니다.
  private void sendNotification(Member member, Post post) {

    NotificationDto notificationDto = new NotificationDto(
        null,
        ADD_WISHLIST_MY_POST,
        member.getNickname() + "님이 " + post.getTitle() + "에 찜을 하였습니다.",
        LocalDateTime.now());
    notificationService.createNotification(post.getMember().getId(), notificationDto);
  }

  // 회원의 찜 목록을 받아오는 메서드입니다.
  public Page<PostPageResponseDto> getWishList(Long memberId, Pageable pageable) {

    Pageable sortedByDate = sortPage.getSortPageable(pageable);

    Page<Wish> wishPosts = wishRepository.findByMemberId(memberId, sortedByDate);

    return wishPosts.map(wish -> {
          Post post = wish.getPost();
          return PostPageResponseDto.fromEntity(post);
        });
  }
}