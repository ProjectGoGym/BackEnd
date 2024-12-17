package com.gogym.post.repository;

import com.gogym.member.entity.Member;
import com.gogym.post.entity.Post;
import com.gogym.post.entity.RecentView;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface RecentViewRepository extends JpaRepository<RecentView, Long> {

  Page<RecentView> findByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);

  boolean existsByMemberAndPost(Member member, Post post);

  Long countByMember(Member member);

  @Modifying
  @Query(value = "DELETE rv FROM recent_views rv JOIN ("
      + "SELECT id FROM recent_views WHERE member_id = :memberId ORDER BY id LIMIT 1"
      + ") tmp ON rv.id = tmp.id", nativeQuery = true)
  void deleteOldestByMember(@Param("authorId") Long memberId);
}