package com.gogym.member.repository;

import com.gogym.member.entity.BanNickname;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BanNicknameRepository extends JpaRepository<BanNickname, Long> {
  boolean existsByBannedNickname(String bannedNickname);
}