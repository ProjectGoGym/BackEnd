package com.gogym.member.controller;

import com.gogym.common.annotation.LoginMemberId;
import com.gogym.member.service.ActivityService;
import com.gogym.post.dto.PostPageResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members/me")
@RequiredArgsConstructor
public class ActivityController {

  private final ActivityService activityService;

  // 내가 작성한 게시글
  @GetMapping("/posts")
  public ResponseEntity<Page<PostPageResponseDto>> getMyPosts(@LoginMemberId Long memberId,
      Pageable pageable) {
    return ResponseEntity.ok(activityService.getMyPosts(memberId, pageable));
  }

  // 내가 찜한 게시글
  @GetMapping("/wishlist")
  public ResponseEntity<Page<PostPageResponseDto>> getMyFavorites(@LoginMemberId Long memberId,
      Pageable pageable) {
    return ResponseEntity.ok(activityService.getMyFavorites(memberId, pageable));
  }

  // 최근 본 게시글
  @GetMapping("/recent-views")
  public ResponseEntity<Page<PostPageResponseDto>> getRecentViews(@LoginMemberId Long memberId,
      Pageable pageable) {
    return ResponseEntity.ok(activityService.getRecentViews(memberId, pageable));
  }
}
