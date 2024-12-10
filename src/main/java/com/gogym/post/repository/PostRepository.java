package com.gogym.post.repository;

import com.gogym.post.entity.Post;
import com.gogym.post.type.PostStatus;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {

  Page<Post> findAllByStatus(Pageable pageable, PostStatus postStatus);

  @Query("SELECT p FROM Post p WHERE p.status = :status AND p.gym.regionId IN :regionIds")
  Page<Post> findAllByStatusAndRegionIds(@Param("status") PostStatus status,
      Pageable sortedByDate, @Param("regionIds") List<Long> regionIds);

  Page<Post> findByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);
}