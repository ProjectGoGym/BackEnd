package com.gogym.post.repository;

import com.gogym.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ViewHistoryRepository extends JpaRepository<Post, Long> {

    // 회원 ID를 기반으로 최근 본 게시글 조회
    Page<Post> findRecentViewsByMemberId(Long memberId, Pageable pageable);
}
