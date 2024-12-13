package com.gogym.member.repository;

import com.gogym.member.entity.KakaoMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KakaoMemberRepository extends JpaRepository<KakaoMember, Long> {
}
