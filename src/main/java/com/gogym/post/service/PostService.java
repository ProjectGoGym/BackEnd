package com.gogym.post.service;

import static com.gogym.exception.ErrorCode.MEMBER_NOT_FOUND;

import com.gogym.exception.CustomException;
import com.gogym.member.entity.Member;
import com.gogym.member.repository.MemberRepository;
import com.gogym.post.dto.PostRequestDto;
import com.gogym.post.dto.PostResponseDto;
import com.gogym.post.entity.Gym;
import com.gogym.post.entity.Post;
import com.gogym.post.repository.GymRepository;
import com.gogym.post.repository.PostRepository;
import com.gogym.region.service.RegionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

  private final PostRepository postRepository;

  private final MemberRepository memberRepository;

  private final GymRepository gymRepository;

  private final RegionService regionService;

  @Transactional
  public PostResponseDto createPost(Long memberId, PostRequestDto postRequestDto) {

    // TODO : 추후 MemberService 에서 추출
    Member member = findByMemberFromMemberRepository(memberId);

    Gym gym = findOrCreateGym(postRequestDto);

    Post post = Post.of(member, gym, postRequestDto);

    postRepository.save(post);

    return PostResponseDto.fromEntity(post);
  }

  private Gym findOrCreateGym(PostRequestDto postRequestDto) {

    Long regionId = regionService.getChildRegionId(postRequestDto.city(),
        postRequestDto.district());

    // 위도와 경도 기준으로 저장된 헬스장이 있는지 확인 후 없으면 새로 생성합니다.
    return gymRepository.findByLatitudeAndLongitude(postRequestDto.latitude(),
            postRequestDto.longitude())
        .orElseGet(() -> gymRepository.save(Gym.createGym(postRequestDto, regionId)));
  }

  // TODO : 추후 MemberService 에서 추출
  private Member findByMemberFromMemberRepository(Long memberId) {

    return memberRepository.findById(memberId)
        .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
  }
}