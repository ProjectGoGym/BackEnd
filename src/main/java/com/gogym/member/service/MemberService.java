package com.gogym.member.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gogym.exception.CustomException;
import com.gogym.exception.ErrorCode;
import com.gogym.member.dto.MemberProfileResponse;
import com.gogym.member.dto.UpdateMemberRequest;
import com.gogym.member.entity.Member;
import com.gogym.member.repository.MemberRepository;
import com.gogym.post.dto.PostResponseDto;
import com.gogym.post.repository.FavoriteRepository;
import com.gogym.post.repository.PostRepository;
import com.gogym.post.repository.ViewHistoryRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

  private final MemberRepository memberRepository;
  private final FavoriteRepository favoriteRepository;
  private final ViewHistoryRepository viewHistoryRepository;
  private final PostRepository postRepository;

  // 이메일로 사용자 조회
  public Member findByEmail(String email) {
    return memberRepository.findByEmail(email)
        .orElseThrow(() -> new CustomException(ErrorCode.EMAIL_NOT_FOUND));
  }

  // ID로 사용자 조회
  public Member findById(Long id) {
    return memberRepository.findById(id)
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
  }

  @Transactional(readOnly = true)
  public MemberProfileResponse getMyProfileById(Long memberId) {
    Member member = findById(memberId);
    return new MemberProfileResponse(member.getEmail(), member.getName(), member.getNickname(),
        member.getPhone(), member.getProfileImageUrl());
  }

  @Transactional
  public void updateMyProfileById(Long memberId, UpdateMemberRequest request) {
    Member member = findById(memberId);
    member.updateProfile(request.name(), request.nickname(), request.phone(),
        request.profileImageUrl());
  }

  @Transactional
  public void deleteMyAccountById(Long memberId) {
    Member member = findById(memberId);
    memberRepository.delete(member); // Soft delete 방식으로 처리 시 변경 필요
  }

  // 내가 작성한 게시글
  public Page<PostResponseDto> getMyPostsById(Long memberId, int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    return postRepository.findByMember_Id(memberId, pageable).map(PostResponseDto::fromEntity);
  }

  // 내가 찜한 게시글
  public Page<PostResponseDto> getMyFavoritesById(Long memberId, int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    return favoriteRepository.findFavoritesByMemberId(memberId, pageable)
        .map(PostResponseDto::fromEntity);
  }

  // 최근 본 게시글
  public Page<PostResponseDto> getRecentViewsById(Long memberId, int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    return viewHistoryRepository.findRecentViewsByMemberId(memberId, pageable)
        .map(PostResponseDto::fromEntity);
  }
}

