package com.gogym.post.repository;

import com.gogym.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteRepository extends JpaRepository<Post, Long> {
    Page<Post> findFavoritesByMemberId(Long memberId, Pageable pageable);
}
