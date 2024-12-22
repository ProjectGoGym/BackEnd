package com.gogym.gympay.repository;

import com.gogym.post.dto.PostPageResponseDto;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransactionRepositoryCustom {

  Page<PostPageResponseDto> getMyTransactions(Long memberId, LocalDateTime startDate,
      LocalDateTime endDate, Pageable pageable, String type);
}
