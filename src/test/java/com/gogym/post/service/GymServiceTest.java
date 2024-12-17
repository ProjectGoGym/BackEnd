package com.gogym.post.service;

import static com.gogym.post.type.MembershipType.MEMBERSHIP_ONLY;
import static com.gogym.post.type.PostType.SELL;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gogym.post.dto.PostRequestDto;
import com.gogym.post.entity.Gym;
import com.gogym.post.repository.GymRepository;
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
class GymServiceTest {

  @Mock
  private GymRepository gymRepository;

  @Mock
  private RegionService regionService;

  @InjectMocks
  private GymService gymService;

  private PostRequestDto postRequestDto;

  @BeforeEach
  void setUp() {
    postRequestDto = new PostRequestDto("제목", "내용", SELL, MEMBERSHIP_ONLY,
        LocalDate.now().plusMonths(10), null, 1000L,
        "url1", "url2", "url3",
        "테스트 헬스장", 1.1, 2.2,
        "url", "도시", "지역");
  }

  @Test
  void 헬스장이_존재하지_않는_경우_헬스장을_저장한다() {
    // given
    when(regionService.getChildRegionId("도시", "지역")).thenReturn(1L);
    when(gymRepository.findByLatitudeAndLongitude(postRequestDto.latitude(), postRequestDto.longitude())).thenReturn(Optional.empty());
    // when
    gymService.findOrCreateGym(postRequestDto);
    // then
    verify(gymRepository).save(any(Gym.class));
  }
}