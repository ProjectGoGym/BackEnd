package com.gogym.post.service;

import com.gogym.member.entity.Member;
import com.gogym.post.dto.PostPageResponseDto;
import com.gogym.post.entity.Post;
import com.gogym.post.entity.RecentView;
import com.gogym.post.repository.RecentViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecentViewService {

  private final RecentViewRepository recentViewRepository;

  private static final int MAX_RECENT_VIEWS = 5;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void saveRecentView(Member member, Post post) {

    // 회원의 최근 본 목록과 새롭게 본 게시글을 비교합니다.
    boolean alreadyExists = recentViewRepository.existsByMemberAndPost(member, post);

    // 이미 본 게시글이면 리턴
    if (alreadyExists) {
      return;
    }

    // 이미 본 게시글이 아니라면 최근 본 목록이 5개 이상이면 가장 옛날 게시글을 삭제합니다.
    ensureRecentViewLimit(member);

    // 새롭게 최근 본 게시글 생성
    RecentView recentView = new RecentView(member, post);
    recentViewRepository.save(recentView);
  }

  @Transactional
  protected void ensureRecentViewLimit(Member member) {

    Long count = recentViewRepository.countByMember(member);

    if (count >= MAX_RECENT_VIEWS) {

      recentViewRepository.deleteOldestByMember(member.getId());
    }
  }

  // 최근 본 게시글 목록을 반환합니다.
  public Page<PostPageResponseDto> getRecentViews(Long memberId, Pageable pageable) {

    Page<RecentView> recentViewPage = recentViewRepository.findByMemberIdOrderByCreatedAtDesc(
        memberId, pageable);

    return recentViewPage.map(recentView -> {
      Post post = recentView.getPost();
      return PostPageResponseDto.fromEntity(post);
    });
  }
}