package com.gogym.member.service;

import com.gogym.post.dto.PostPageResponseDto;
import com.gogym.post.service.PostService;
import com.gogym.post.service.RecentViewService;
import com.gogym.post.service.WishService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActivityService {

  private final PostService postService;
  private final WishService wishService;
  private final MemberService memberService;
  private final RecentViewService recentViewService;

  // 내가 작성한 게시글 조회
  public Page<PostPageResponseDto> getMyPosts(Long memberId, Pageable pageable) {
    memberService.findById(memberId);
    return postService.getAuthorPosts(memberId, pageable);
  }

  // 내가 찜한 게시글 조회
  public Page<PostPageResponseDto> getMyFavorites(Long memberId, Pageable pageable) {
    memberService.findById(memberId);
    return wishService.getWishList(memberId, pageable);
  }

  // 최근 본 게시글 조회
  public Page<PostPageResponseDto> getRecentViews(Long memberId, Pageable pageable) {
    memberService.findById(memberId);
    return recentViewService.getRecentViews(memberId, pageable);
  }

}


