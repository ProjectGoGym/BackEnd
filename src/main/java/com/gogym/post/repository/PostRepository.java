package com.gogym.post.repository;

import com.gogym.post.entity.Post;
import com.gogym.post.type.PostStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {

  Page<Post> findAllByStatusOrderByCreatedAtDesc(Pageable pageable, PostStatus postStatus);

  @Query("SELECT p FROM Post p WHERE p.status = :status AND p.gym.regionId IN :regionIds ORDER BY p.createdAt DESC")
  Page<Post> findAllByStatusAndRegionIds(@Param("status") PostStatus status,
      Pageable sortedByDate, @Param("regionIds") List<Long> regionIds);

  Page<Post> findByAuthorIdOrderByCreatedAtDesc(Long authorId, Pageable pageable);

  @Query("SELECT p.status FROM Post p WHERE p.id = :postId")
  Optional<PostStatus> findStatusByPostId(@Param("postId") Long postId);
  
  Page<Post> findByAuthorId(Long authorId, Pageable pageable);
  Page<Post> findFavoritesByMemberId(Long memberId, Pageable pageable);
  Page<Post> findRecentViewsByMemberId(Long memberId, Pageable pageable);
}