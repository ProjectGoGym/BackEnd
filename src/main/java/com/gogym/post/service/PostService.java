package com.gogym.post.service;

import static com.gogym.exception.ErrorCode.DELETED_POST;
import static com.gogym.exception.ErrorCode.MEMBER_NOT_FOUND;
import static com.gogym.exception.ErrorCode.POST_NOT_FOUND;
import static com.gogym.post.type.PostStatus.HIDDEN;
import static com.gogym.post.type.PostStatus.POSTING;

import com.gogym.exception.CustomException;
import com.gogym.member.entity.Member;
import com.gogym.member.service.MemberService;
import com.gogym.post.dto.PostFilterRequestDto;
import com.gogym.post.dto.PostPageResponseDto;
import com.gogym.post.dto.PostRequestDto;
import com.gogym.post.dto.PostResponseDto;
import com.gogym.post.entity.Gym;
import com.gogym.post.entity.Post;
import com.gogym.post.repository.PostRepository;
import com.gogym.post.repository.PostRepositoryCustom;
import com.gogym.region.dto.RegionResponseDto;
import com.gogym.region.service.RegionService;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

  private final PostRepository postRepository;

  private final MemberService memberService;

  private final GymService gymService;

  private final RegionService regionService;

  private final PostRepositoryCustom postRepositoryCustom;

  @Transactional
  public PostResponseDto createPost(Long memberId, PostRequestDto postRequestDto) {

    Member member = memberService.findById(memberId);

    Gym gym = gymService.findOrCreateGym(postRequestDto);

    Post post = Post.of(member, gym, postRequestDto);

    postRepository.save(post);

    RegionResponseDto regionResponseDto = regionService.findById(post.getGym().getRegionId());

    return PostResponseDto.fromEntity(post, regionResponseDto);
  }

  // 회원 ID 가 null 이면 비회원, null 이 아니면 회원이 게시글 목록을 보는 상황입니다.
  public Page<PostPageResponseDto> getAllPosts(Long memberId, Pageable pageable) {

    List<Long> regionIds = memberId != null ? getRegionIds(memberId) : null;

    return fetchPosts(regionIds, pageable);
  }

  // 회원과 비회원의 기준으로 보여주는 게시글을 찾습니다. 회원의 경우 설정된 관심지역을 기준으로 보여집니다.
  private Page<PostPageResponseDto> fetchPosts(List<Long> regionIds, Pageable pageable) {

    Pageable sortedByDate = getSortPageable(pageable);

    Page<Post> posts = regionIds == null
        ? postRepository.findAllByStatus(sortedByDate, POSTING)
        : postRepository.findAllByStatusAndRegionIds(POSTING, sortedByDate, regionIds);

    return posts != null ? posts.map(PostPageResponseDto::fromEntity) : Page.empty();
  }

  // 회원 ID 가 null 이면 비회원, null 이 아니면 회원이 게시글을 필터링 하는 상황입니다.
  public Page<PostPageResponseDto> getFilterPosts(Long memberId,
      PostFilterRequestDto postFilterRequestDto, Pageable pageable) {

    List<Long> regionIds = memberId != null ? getRegionIds(memberId) : null;

    return fetchFilterPosts(regionIds, postFilterRequestDto, pageable);
  }

  // 필터링 적용 메서드 입니다.
  private Page<PostPageResponseDto> fetchFilterPosts(List<Long> regionIds,
      PostFilterRequestDto postFilterRequestDto,
      Pageable pageable) {

    Pageable sortedByDate = getSortPageable(pageable);

    // 필터링 설정
    Page<Post> filteredPosts = postRepositoryCustom.findAllWithFilter(regionIds,
        postFilterRequestDto, sortedByDate);

    return filteredPosts != null ? filteredPosts.map(PostPageResponseDto::fromEntity)
        : Page.empty();
  }

  // 게시글의 상세 페이지를 조회합니다.
  public PostResponseDto getDetailPost(Long postId) {

    Post post = findById(postId);

    // 숨김처리 된 게시글은 조회가 불가능합니다.
    if (post.getStatus() == HIDDEN) {
      throw new CustomException(DELETED_POST);
    }

    RegionResponseDto regionResponseDto = regionService.findById(post.getGym().getRegionId());

    return PostResponseDto.fromEntity(post, regionResponseDto);
  }

  // 회원의 경우 설정된 관심지역을 가져옵니다.
  private List<Long> getRegionIds(Long memberId) {

    Member member = memberService.findById(memberId);

    List<Long> regionIds = Stream.of(member.getRegionId1(), member.getRegionId2())
        .filter(Objects::nonNull)
        .toList();

    return regionIds.isEmpty() ? null : regionIds;
  }

  // 게시글이 생성된 시간(날짜) 기준으로 역정렬 합니다.
  private Pageable getSortPageable(Pageable pageable) {
    return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
        Sort.by(Direction.DESC, "createdAt"));
  }

  // 주어진 게시글 ID 로 게시글을 찾습니다.
  public Post findById(Long postId) {

    return postRepository.findById(postId).orElseThrow(() -> new CustomException(POST_NOT_FOUND));
  }

  // 채팅방 에서 호출할 메서드입니다. 게시글 작성자를 찾습니다.
  public Member getPostAuthor(Long postId) {

    if (findById(postId).getMember() == null) {
      throw new CustomException(MEMBER_NOT_FOUND);
    } else {
      return findById(postId).getMember();
    }
  }

  // 회원 본인이 작성한 게시글을 찾는 로직입니다.
  public Page<Post> findByMemberId(Long memberId, Pageable pageable) {

    return postRepository.findByMemberIdOrderByCreatedAtDesc(memberId, pageable);
  }
}