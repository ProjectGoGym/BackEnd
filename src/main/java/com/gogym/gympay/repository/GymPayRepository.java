package com.gogym.gympay.repository;

import com.gogym.gympay.entity.GymPay;
import com.gogym.member.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GymPayRepository extends JpaRepository<GymPay, Long> {

  Optional<GymPay> findByMember(Member member);
}
