package com.gogym.post.repository;

import com.gogym.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;

public interface PostRepository extends JpaRepository<Post, Long> {
  // Member의 ID를 기준으로 Post를 조회
  Page<Post> findByMember_Id(Long memberId, Pageable pageable);
}
