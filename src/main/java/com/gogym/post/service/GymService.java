package com.gogym.post.service;

import com.gogym.post.dto.PostRequestDto;
import com.gogym.post.entity.Gym;
import com.gogym.post.repository.GymRepository;
import com.gogym.region.service.RegionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GymService {

  private final GymRepository gymRepository;

  private final RegionService regionService;

  public Gym findOrCreateGym(PostRequestDto postRequestDto) {

    Long regionId = regionService.getChildRegionId(postRequestDto.city(),
        postRequestDto.district());

    // 위도와 경도 기준으로 저장된 헬스장이 있는지 확인 후 없으면 새로 생성합니다.
    return gymRepository.findByLatitudeAndLongitude(postRequestDto.latitude(),
            postRequestDto.longitude())
        .orElseGet(() -> gymRepository.save(Gym.createGym(postRequestDto, regionId)));
  }
}
