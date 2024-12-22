package com.gogym.gympay.repository;

import com.gogym.gympay.dto.response.GetHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GymPayHistoryRepositoryCustom {

  Page<GetHistory> getAllHistoriesByGymPayId(Long id, Pageable pageable);

}
