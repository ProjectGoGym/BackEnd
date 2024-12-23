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

  Page<Post> findAllByStatusInOrderByCreatedAtDesc(List<PostStatus> postStatuses, Pageable pageable);

  @Query("SELECT p FROM Post p WHERE p.status IN :statuses AND p.gym.regionId IN :regionIds ORDER BY p.createdAt DESC")
  Page<Post> findAllByStatusInAndRegionIds(@Param("statuses") List<PostStatus> postStatuses,
      Pageable sortedByDate, @Param("regionIds") List<Long> regionIds);

  Page<Post> findByAuthorIdOrderByCreatedAtDesc(Long authorId, Pageable pageable);

  @Query("SELECT p.status FROM Post p WHERE p.id = :postId")
  Optional<PostStatus> findStatusByPostId(@Param("postId") Long postId);

  @Query("""
      SELECT COUNT(c) > 0
      FROM Post p
      JOIN p.chatRoom c
      JOIN Transaction t ON c.transactionId = t.id
      WHERE p.id = :postId
      AND t.status IN ('COMPLETED', 'STARTED')
  """)
  boolean existsChatRoomWithTransactionInProgressOrCompleted(@Param("postId") Long postId);
}