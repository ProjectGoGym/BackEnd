package com.gogym.post.repository;

import com.gogym.member.entity.Member;
import com.gogym.post.entity.Post;
import com.gogym.post.entity.Wish;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishRepository extends JpaRepository<Wish, Long> {

  Optional<Wish> findByMemberAndPost(Member member, Post post);

  Page<Wish> findByMemberId(Long memberId, Pageable pageable);
}