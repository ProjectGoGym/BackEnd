package com.gogym.member.controller;

import com.gogym.common.annotation.LoginMemberId;
import com.gogym.gympay.service.TransactionService;
import com.gogym.member.service.ActivityService;
import com.gogym.post.dto.PostPageResponseDto;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members/me")
@RequiredArgsConstructor
public class ActivityController {

  private final ActivityService activityService;
  private final TransactionService transactionService;

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

  @GetMapping("/transactions/{type}")
  public ResponseEntity<Page<PostPageResponseDto>> getMyTransactions(@LoginMemberId Long memberId,
      @PathVariable String type,
      @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
      @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate,
      Pageable pageable) {
    return ResponseEntity.ok(transactionService.getMyTransactions(memberId, startDate, endDate, pageable, type));
  }
}
