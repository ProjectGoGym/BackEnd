package com.gogym.member.service;

import com.gogym.exception.CustomException;
import com.gogym.exception.ErrorCode;
import com.gogym.member.entity.Member;
import com.gogym.post.dto.PostPageResponseDto;
import com.gogym.post.service.PostService;
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

  // 내가 작성한 게시글 조회
  public Page<PostPageResponseDto> getMyPosts(Long memberId, Pageable pageable) {
    validateMember(memberId);
    return postService.getAuthorPosts(memberId, pageable);
  }

  // 내가 찜한 게시글 조회
  public Page<PostPageResponseDto> getMyFavorites(Long memberId, Pageable pageable) {
    validateMember(memberId);
    return wishService.getWishList(memberId, pageable);
  }

  // 최근 본 게시글 조회
  public Page<PostPageResponseDto> getRecentViews(Long memberId, Pageable pageable) {
    validateMember(memberId);
    return postService.getAuthorPosts(memberId, pageable);
  }

  // 멤버 객체 검증
  private void validateMember(Long memberId) {
    Member member = memberService.findById(memberId);
    if (member == null || !member.isActive()) {
      throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
    }
  }
}


