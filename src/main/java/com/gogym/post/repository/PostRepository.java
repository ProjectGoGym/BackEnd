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

  // 상태별 게시글 조회
  Page<Post> findAllByStatus(Pageable pageable, PostStatus postStatus);

  // 지역 및 상태별 게시글 조회
  @Query("SELECT p FROM Post p WHERE p.status = :status AND p.gym.regionId IN :regionIds")
  Page<Post> findAllByStatusAndRegionIds(@Param("status") PostStatus status, Pageable sortedByDate,
      @Param("regionIds") List<Long> regionIds);

  // 특정 회원이 작성한 게시글 조회
  Page<Post> findByMember_Id(Long memberId, Pageable pageable);

  // 찜한 게시글 조회
  @Query("SELECT p FROM Post p JOIN p.favorites f WHERE f.memberId = :memberId")
  Page<Post> findFavoritesByMemberId(@Param("memberId") Long memberId, Pageable pageable);

  // 최근 본 게시글 조회
  @Query("SELECT p FROM Post p JOIN p.viewHistories v WHERE v.memberId = :memberId ORDER BY v.viewedAt DESC")
  Page<Post> findRecentViewsByMemberId(@Param("memberId") Long memberId, Pageable pageable);
}
