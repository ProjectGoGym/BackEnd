package com.gogym.member.repository;

import com.gogym.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
 
  boolean existsByEmail(String email);

  boolean existsByNickname(String nickname);

  Optional<Member> findByEmail(String email);
}
