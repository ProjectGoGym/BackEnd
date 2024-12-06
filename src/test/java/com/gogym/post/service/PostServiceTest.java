package com.gogym.post.service;

import static com.gogym.exception.ErrorCode.MEMBER_NOT_FOUND;
import static com.gogym.post.type.MembershipType.MEMBERSHIP_ONLY;
import static com.gogym.post.type.PostType.SELL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gogym.exception.CustomException;
import com.gogym.member.entity.Member;
import com.gogym.member.repository.MemberRepository;
import com.gogym.post.dto.PostRequestDto;
import com.gogym.post.entity.Gym;
import com.gogym.post.repository.GymRepository;
import com.gogym.post.repository.PostRepository;
import com.gogym.region.entity.Region;
import com.gogym.region.service.RegionService;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

  @Mock
  private PostRepository postRepository;

  @Mock
  private MemberRepository memberRepository;

  @Mock
  private GymRepository gymRepository;

  @Mock
  private RegionService regionService;

  @InjectMocks
  private PostService postService;

  private Member member;
  private Gym gym;
  private Region parent;
  private Region child;
  private PostRequestDto postRequestDto;

  @BeforeEach
  void setUp() {

    member = Member.builder()
        .id(1L)
        .build();
    parent = Region.builder()
        .id(1L)
        .name("도시")
        .build();
    child = Region.builder()
        .id(2L)
        .name("지역")
        .build();
    gym = Gym.builder()
        .gymName("테스트 헬스장")
        .latitude(1.1)
        .longitude(2.2)
        .regionId(parent.getId())
        .build();
    postRequestDto = new PostRequestDto("제목", "내용", SELL, MEMBERSHIP_ONLY,
        LocalDate.now().plusMonths(10), null, 1000L,
        "url1", "url2", "url3",
        "테스트 헬스장", 1.1, 2.2,
        "url", "도시", "지역");
  }

  @Test
  void 헬스장이_존재하는_경우_성공적으로_게시글을_작성한다() {
    // given
    when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
    when(gymRepository.findByLatitudeAndLongitude(postRequestDto.latitude(),
        postRequestDto.longitude())).thenReturn(Optional.of(gym));
    // when
    postService.createPost(member.getId(), postRequestDto);
    // then
    verify(postRepository).save(any());
  }

  @Test
  void 헬스장이_존재하지_않는_경우_헬스장을_저장한다() {
    // given
    when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));

    when(gymRepository.findByLatitudeAndLongitude(postRequestDto.latitude(),
        postRequestDto.longitude())).thenReturn(Optional.empty());
    when(gymRepository.save(any(Gym.class))).thenReturn(gym);
    // when
    postService.createPost(member.getId(), postRequestDto);
    // then
    verify(gymRepository).save(any(Gym.class));
    verify(postRepository).save(any());
  }

  @Test
  void 회원이_없으면_예외가_발생한다() {
    // given
    when(memberRepository.findById(member.getId())).thenReturn(Optional.empty());
    // when
    CustomException e = assertThrows(CustomException.class,
        () -> postService.createPost(member.getId(), postRequestDto));
    // then
    assertEquals(e.getMessage(), "회원을 찾을 수 없습니다.");
    assertEquals(e.getErrorCode(), MEMBER_NOT_FOUND);
  }
}