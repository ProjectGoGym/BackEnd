package com.gogym.post.type;

import lombok.Getter;

@Getter
public enum PostStatus {

  // 게시중(거래대기), 거래중, 거래완료, 숨김처리 등
  PENDING("거래대기"), IN_PROGRESS("거래중"), COMPLETED("거래완료"), HIDDEN("숨김처리");

  private final String statusName;

  PostStatus(String statusName) {
    this.statusName = statusName;
  }
}