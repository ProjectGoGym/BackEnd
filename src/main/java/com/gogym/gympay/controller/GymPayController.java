package com.gogym.gympay.controller;

import com.gogym.common.annotation.LoginMemberId;
import com.gogym.gympay.dto.response.GetHistory;
import com.gogym.gympay.service.GymPayHistoryService;
import com.gogym.gympay.service.GymPayService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/gym-pays")
public class GymPayController {

  private final GymPayService gymPayService;
  private final GymPayHistoryService gymPayHistoryService;

  @PostMapping
  public ResponseEntity<Long> open(@LoginMemberId Long memberId) {
    gymPayService.save(memberId);

    return ResponseEntity.noContent().build();
  }

  @GetMapping("/histories")
  public ResponseEntity<Page<GetHistory>> getHistories(@LoginMemberId Long memberId,
      @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
      @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate,
      Pageable pageable) {
    var histories = gymPayHistoryService.getHistories(memberId, startDate, endDate,
        pageable);

    return ResponseEntity.ok(histories);
  }
}
