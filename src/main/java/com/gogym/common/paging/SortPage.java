package com.gogym.common.paging;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;

@Component
public class SortPage {

  // 게시글이 생성된 시간(날짜) 기준으로 역정렬 합니다.
  public Pageable getSortPageable(Pageable pageable) {
    return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
        Sort.by(Direction.DESC, "createdAt"));
  }
}
