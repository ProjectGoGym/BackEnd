package com.gogym.post.service;

import static com.gogym.exception.ErrorCode.ALREADY_TRANSACTION;
import static com.gogym.exception.ErrorCode.CHATROOM_NOT_FOUND;
import static com.gogym.exception.ErrorCode.DELETED_POST;
import static com.gogym.exception.ErrorCode.FORBIDDEN;
import static com.gogym.exception.ErrorCode.POST_NOT_FOUND;
import static com.gogym.exception.ErrorCode.REQUEST_VALIDATION_FAIL;
import static com.gogym.post.type.PostStatus.HIDDEN;
import static com.gogym.post.type.PostStatus.IN_PROGRESS;
import static com.gogym.post.type.PostStatus.PENDING;
import static com.gogym.post.type.PostType.SELL;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gogym.chat.entity.ChatRoom;
import com.gogym.exception.CustomException;
import com.gogym.gympay.entity.Transaction;
import com.gogym.gympay.service.TransactionService;
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
import com.gogym.post.type.PostStatus;
import com.gogym.region.dto.RegionResponseDto;
import com.gogym.region.service.RegionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

  private static final Logger log = LoggerFactory.getLogger(PostService.class);
  private final PostRepository postRepository;

  private final MemberService memberService;

  private final GymService gymService;

  private final RegionService regionService;

  private final PostRepositoryCustom postRepositoryCustom;

  private final RecentViewService recentViewService;

  private final TransactionService transactionService;
  
  private final PostQueryService postQueryService;

  private static final List<PostStatus> postStatuses = List.of(PENDING, IN_PROGRESS);

  @Transactional
  public PostResponseDto createPost(Long memberId, PostRequestDto postRequestDto) {

    Member member = memberService.findById(memberId);

    Gym gym = gymService.findOrCreateGym(postRequestDto);

    Post post = Post.of(member, gym, postRequestDto);

    postRepository.save(post);

    RegionResponseDto regionResponseDto = regionService.findById(post.getGym().getRegionId());

    return PostResponseDto.fromEntity(post, regionResponseDto, false);
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
              postRepository.findAllByStatusInOrderByCreatedAtDesc(postStatuses, pageable)
              : postRepository.findAllByStatusInAndRegionIds(postStatuses, pageable, regionIds);
    } else {

      posts = postRepositoryCustom.findAllWithFilter(regionIds, postFilterRequestDto, pageable);
    }

    return returnPosts(posts);
  }

  // 게시글의 상세 페이지를 조회합니다. 비회원의 경우 읽기 처리만 하고, 회원의 경우 최근본 게시글로 저장이 됩니다.
  public PostResponseDto getDetailPost(Long memberId, Long postId) {

    //Post post = findById(postId);
    Post post = postQueryService.findById(postId);

    // 숨김처리 된 게시글은 조회가 불가능합니다.
    if (post.getStatus() == HIDDEN) {
      throw new CustomException(DELETED_POST);
    }

    // 최근 본 게시글은 저장처리 됩니다.
    if (memberId != null) {
      Member member = memberService.findById(memberId);

      recentViewService.saveRecentView(member, post);
    }

    boolean isWished = postQueryService.isWished(post, memberId);

    RegionResponseDto regionResponseDto = regionService.findById(post.getGym().getRegionId());

    return PostResponseDto.fromEntity(post, regionResponseDto, isWished);
  }

  // 게시글 수정 메서드입니다
  @Transactional
  public PostResponseDto updatePost(Long memberId, Long postId,
      PostUpdateRequestDto postUpdateRequestDto) {

    Member member = memberService.findById(memberId);

    Post post = postQueryService.findById(postId);

    boolean isWished = postQueryService.isWished(post, memberId);

    validatePostAuthor(member, post);

    // 게시글 상태는 게시중(거래대기) 또는 숨김처리(삭제) 만 변경할 수 있습니다.
    if (postUpdateRequestDto.status() != PENDING && postUpdateRequestDto.status() != HIDDEN) {
      throw new CustomException(REQUEST_VALIDATION_FAIL);
    }

    post.update(postUpdateRequestDto);

    // 게시글의 전체 필드를 반환합니다.
    RegionResponseDto regionResponseDto = regionService.findById(post.getGym().getRegionId());

    return PostResponseDto.fromEntity(post, regionResponseDto, isWished);
  }

  // 회원의 경우 설정된 관심지역을 가져옵니다.
  private List<Long> getRegionIds(Long memberId) {

    Member member = memberService.findById(memberId);

    List<Long> regionIds = Stream.of(member.getRegionId1(), member.getRegionId2())
        .filter(Objects::nonNull)
        .toList();

    return regionIds.isEmpty() ? null : regionIds;
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

  // 게시글 아이디로 게시글 상태를 반환하는 메서드 입니다.
  public PostStatus getPostStatus(Long postId) {

    return postRepository.findStatusByPostId(postId)
        .orElseThrow(() -> new CustomException(POST_NOT_FOUND));
  }

  // 게시글의 상태 변경을 전용으로 하는 메서드입니다.
  @Transactional
  public void changePostStatus(Long memberId, Long postId, Long chatRoomId, PostStatus status) {

    Member member = memberService.findById(memberId);
    //Post post = findById(postId);
    Post post = postQueryService.findById(postId);
    ChatRoom chatRoom = getChatRoom(post, chatRoomId);

    validatePostAuthor(member, post);

    // Seller 와 Buyer 를 설정 합니다.
    Pair<Member, Member> role = getRole(post, chatRoom);
    Member seller = role.getFirst();
    Member buyer = role.getSecond();

    // 요청값이 현재 변경할 수 있는 값이 아니면 예외를 던집니다.
    if (!isValidStatus(post.getStatus(), status)) {

      throw new CustomException(REQUEST_VALIDATION_FAIL,
          String.format("'%s' 상태는 '%s' 상태로 변경할 수 없습니다.",
              post.getStatus().getStatusName(), status.getStatusName()));
    }

    Long transactionId = chatRoom.getTransactionId();

    Transaction transaction =
        transactionId == null ? null : transactionService.getById(transactionId);

    post.updateStatus(status);

    handleTransactionStatus(status, post, transaction, chatRoom, seller, buyer);
  }

  private Pair<Member, Member> getRole(Post post, ChatRoom chatRoom) {

    return post.getPostType() == SELL ?
        Pair.of(post.getAuthor(), chatRoom.getRequestor())
        : Pair.of(chatRoom.getRequestor(), post.getAuthor());
  }

  // 현재 상태값과 새로운 상태값을 검증하는 메서드 입니다.
  private boolean isValidStatus(PostStatus currentStatus, PostStatus newStatus) {

    return switch (currentStatus) {
      case PENDING -> newStatus == IN_PROGRESS;
      case IN_PROGRESS -> newStatus == PostStatus.COMPLETED || newStatus == PENDING;
      case COMPLETED, HIDDEN -> false;
    };
  }

  // 게시글의 상태값에 따라 transactionService 를 실행하는 메서드 입니다.
  private void handleTransactionStatus(PostStatus status, Post post, Transaction transaction,
      ChatRoom chatRoom, Member seller, Member buyer) {

    switch (status) {
      case IN_PROGRESS -> {

        // 현재 게시글의 다른 거래가 있는 지 확인합니다.
        validateTransactionStatus(post);

        if (transaction == null) {
          transactionService.start(chatRoom, seller, buyer);
        } else {
          transactionService.restart(transaction);
        }
      }

      case PENDING -> transactionService.cancel(transaction);
      case COMPLETED -> transactionService.complete(transaction);
    }
  }

  private void validateTransactionStatus(Post post) {

    boolean validateTransactionStatus = postRepository.existsChatRoomWithTransactionInProgressOrCompleted(
        post.getId());

    if (validateTransactionStatus) {
      throw new CustomException(ALREADY_TRANSACTION);
    }
  }

  // 게시글에서 채팅방의 존재를 확인하는 메서드 입니다.
  private ChatRoom getChatRoom(Post post, Long chatRoomId) {
    return post.getChatRoom().stream()
        .filter(room -> room.getId().equals(chatRoomId))
        .findFirst()
        .orElseThrow(() -> new CustomException(CHATROOM_NOT_FOUND));
  }

  // 게시글 수정 시 게시글 작성자 보인인지 확인하는 메서드 입니다.
  private void validatePostAuthor(Member member, Post post) {

    if (!member.equals(post.getAuthor())) {
      throw new CustomException(FORBIDDEN);
    }
  }
}