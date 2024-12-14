package com.gogym.post.service;

import static com.gogym.exception.ErrorCode.DELETED_POST;
import static com.gogym.exception.ErrorCode.FORBIDDEN;
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
import com.gogym.post.dto.PostUpdateRequestDto;
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
import org.springframework.data.domain.Pageable;
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

  private final RecentViewService recentViewService;

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

    return fetchPostsBase(regionIds, pageable, null);
  }

  // 회원 ID 가 null 이면 비회원, null 이 아니면 회원이 게시글을 필터링 하는 상황입니다.
  public Page<PostPageResponseDto> getFilterPosts(Long memberId,
      PostFilterRequestDto postFilterRequestDto, Pageable pageable) {

    List<Long> regionIds = memberId != null ? getRegionIds(memberId) : null;

    return fetchPostsBase(regionIds, pageable, postFilterRequestDto);
  }

  private Page<PostPageResponseDto> fetchPostsBase(List<Long> regionIds, Pageable pageable,
      PostFilterRequestDto postFilterRequestDto) {

    Page<Post> posts;

    if (postFilterRequestDto == null) {
      posts =
          regionIds == null ?
              postRepository.findAllByStatusOrderByCreatedAtDesc(pageable, POSTING)
              : postRepository.findAllByStatusAndRegionIds(POSTING, pageable, regionIds);
    } else {

      posts = postRepositoryCustom.findAllWithFilter(regionIds, postFilterRequestDto, pageable);
    }

    return returnPosts(posts);
  }

  // 게시글의 상세 페이지를 조회합니다. 비회원의 경우 읽기 처리만 하고, 회원의 경우 최근본 게시글로 저장이 됩니다.
  public PostResponseDto getDetailPost(Long memberId, Long postId) {

    Post post = findById(postId);

    // 숨김처리 된 게시글은 조회가 불가능합니다.
    if (post.getStatus() == HIDDEN) {
      throw new CustomException(DELETED_POST);
    }

    // 최근 본 게시글은 저장처리 됩니다.
    if (memberId != null) {
      Member member = memberService.findById(memberId);

      recentViewService.saveRecentView(member, post);
    }

    RegionResponseDto regionResponseDto = regionService.findById(post.getGym().getRegionId());

    return PostResponseDto.fromEntity(post, regionResponseDto);
  }

  // 게시글 수정 메서드입니다
  @Transactional
  public PostResponseDto updatePost(Long memberId, Long postId,
      PostUpdateRequestDto postUpdateRequestDto) {

    Member member = memberService.findById(memberId);

    Post post = findById(postId);

    // 게시글 작성자만 게시글 수정 권한이 있습니다
    if (post.getAuthor() != member) {
      throw new CustomException(FORBIDDEN);
    }

    post.update(postUpdateRequestDto);

    // 게시글의 전체 필드를 반환합니다.
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

  // 주어진 게시글 ID 로 게시글을 찾습니다.
  public Post findById(Long postId) {

    return postRepository.findById(postId).orElseThrow(() -> new CustomException(POST_NOT_FOUND));
  }

  // 채팅방 에서 호출할 메서드입니다. 게시글 작성자를 찾습니다.
  public Member getPostAuthor(Long postId) {

    Post post = findById(postId);

    if (post.getAuthor() == null) {
      throw new CustomException(MEMBER_NOT_FOUND);
    } else {
      return post.getAuthor();
    }
  }

  // 특정 회원의 아이디로 특정 회원의 게시글 목록을 반환하는 메서드 입니다.
  public Page<PostPageResponseDto> getAuthorPosts(Long authorId, Pageable pageable) {

    Page<Post> posts = postRepository.findByAuthorIdOrderByCreatedAtDesc(
        authorId, pageable);

    return returnPosts(posts);
  }

  // 게시글 페이징 공통처리 메서드 입니다.
  private Page<PostPageResponseDto> returnPosts(Page<Post> posts) {

    return posts != null ? posts.map(PostPageResponseDto::fromEntity) : Page.empty();
  }
}