package com.gogym.region.service;

import static com.gogym.exception.ErrorCode.CITY_NOT_FOUND;
import static com.gogym.exception.ErrorCode.DISTRICT_NOT_FOUND;

import com.gogym.exception.CustomException;
import com.gogym.region.dto.RegionDto;
import com.gogym.region.dto.RegionResponseDto;
import com.gogym.region.entity.Region;
import com.gogym.region.repository.RegionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegionService {

  private final RegionRepository regionRepository;

  // 회원가입 시 관심지역 설정으로 사용, 반환값은 부모노드의 하위 자식 노드들 이며 저장은 자식노드의 ID 값을 저장하면 될 것 같습니다.
  public List<RegionDto> getRegions(String name) {

    Region region =
        regionRepository.findByName(name).orElseThrow(() -> new CustomException(CITY_NOT_FOUND));

    // 하위지역이 없는경우(예 : 세종특별자치시) 부모노드의 아이디와 이름이 내려갑니다.
    if (region.getChildren() == null || region.getChildren().isEmpty()) {
      return List.of(RegionDto.fromEntity(region));
    }

    return region.getChildren().stream().map(RegionDto::fromEntity).toList();
  }

  // 지도 API 에서 추출한 상위 노드와 하위 노드를 DB 와 비교하여 지역 ID 값을 추출합니다.
  public Long getChildRegionId(String city, String district) {

    Region parent =
        regionRepository.findByName(city).orElseThrow(() -> new CustomException(CITY_NOT_FOUND));

    Region child = regionRepository.findByNameAndParentId(district, parent.getId())
        .orElseThrow(() -> new CustomException(DISTRICT_NOT_FOUND));

    return child.getId();
  }

  // 지역 ID 값으로 부모 노드의 이름과 자식 노드의 이름을 추출합니다.
  public RegionResponseDto findById(Long regionId) {

    Region region =
        regionRepository.findById(regionId).orElseThrow(() -> new CustomException(CITY_NOT_FOUND));

    if (region.getDepth() == 2) {
      return new RegionResponseDto(null, region.getName());
    }

    return new RegionResponseDto(region.getParent().getName(), region.getName());
  }
}
