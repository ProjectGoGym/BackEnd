package com.gogym.gympay.repository;

import com.gogym.gympay.entity.GymPayHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GymPayHistoryRepository extends JpaRepository<GymPayHistory, Long>, GymPayHistoryRepositoryCustom {

}
