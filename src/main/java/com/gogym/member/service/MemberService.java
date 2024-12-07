package com.gogym.member.service;

import com.gogym.exception.CustomException;
import com.gogym.exception.ErrorCode;
// import com.gogym.member.dto.MemberProfileResponse;
// import com.gogym.member.dto.UpdateMemberRequest;
import com.gogym.member.entity.Member;
import com.gogym.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

  private final MemberRepository memberRepository;

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

  /*
   * 
   * // 내 정보 조회 public MemberProfileResponse getMyProfileById(Long memberId) { Member member =
   * findById(memberId); return new MemberProfileResponse( member.getEmail(), member.getName(),
   * member.getNickname(), member.getPhone(), member.getProfileImageUrl() ); }
   * 
   * // 내 정보 수정
   * 
   * @Transactional public void updateMyProfileById(Long memberId, UpdateMemberRequest request) {
   * Member member = findById(memberId); member.updateProfile(request.getName(),
   * request.getNickname(), request.getPhone(), request.getProfileImageUrl()); }
   */
  // 회원 탈퇴
  @Transactional
  public void deleteMyAccountById(Long memberId) {
    Member member = findById(memberId);
    memberRepository.delete(member);
  }


  // TODO - 아래 전부 페이징 처리해야함
  // 내가 작성한 게시글 조회
  public List<String> getMyPostsById(Long memberId, int page, int size) {
    Member member = findById(memberId);
    Pageable pageable = PageRequest.of(page, Math.min(size, 5));
    // TODO 내가 작성한 게시글 로직 유노님이랑 구현해야하는 부분
    return List.of("Post 1", "Post 2", "Post 3", "Post 4", "Post 5");
  }

  // 내가 찜한 게시글 조회
  public List<String> getMyFavoritesById(Long memberId, int page, int size) {
    Member member = findById(memberId);
    Pageable pageable = PageRequest.of(page, Math.min(size, 5));
    // TODO 찜한 게시글 로직 유노님이랑 구현해야하는 부분
    return List.of("Favorite 1", "Favorite 2", "Favorite 3", "Favorite 4", "Favorite 5");
  }

  // 최근 본 게시글 조회
  public List<String> getRecentViewsById(Long memberId, int page, int size) {
    Member member = findById(memberId);
    Pageable pageable = PageRequest.of(page, Math.min(size, 5));
    // TODO 최근 본 게시글 로직 유노님이랑 구현해야하는 부분
    return List.of("View 1", "View 2", "View 3", "View 4", "View 5");
  }

}


