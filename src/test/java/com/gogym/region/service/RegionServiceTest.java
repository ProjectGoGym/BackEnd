package com.gogym.region.service;

import static com.gogym.exception.ErrorCode.CITY_NOT_FOUND;
import static com.gogym.exception.ErrorCode.DISTRICT_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.gogym.exception.CustomException;
import com.gogym.region.dto.RegionDto;
import com.gogym.region.dto.RegionResponseDto;
import com.gogym.region.entity.Region;
import com.gogym.region.repository.RegionRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegionServiceTest {

  @Mock
  private RegionRepository regionRepository;

  @InjectMocks
  private RegionService regionService;

  private Region seoulParent;
  private Region seoulChild1;
  private Region seoulChild2;
  private Region seoulChild3;

  private Region sejongParent;

  @BeforeEach
  void setUp() {
    seoulParent = new Region(1L, "서울특별시", null, new ArrayList<>(), 0L);
    seoulChild1 = new Region(2L, "강남구", seoulParent, new ArrayList<>(), 1L);
    seoulChild2 = new Region(3L, "강서구", seoulParent, new ArrayList<>(), 1L);
    seoulChild3 = new Region(4L, "강동구", seoulParent, new ArrayList<>(), 1L);

    sejongParent = new Region(5L, "세종특별자치시", null, new ArrayList<>(), 0L);

    seoulParent.getChildren().addAll(List.of(seoulChild1, seoulChild2, seoulChild3));
  }

  @Test
  void 상위지역을_입력하면_하위지역이_반환된다() {
    // given
    String name = "서울특별시";
    when(regionRepository.findByName(name)).thenReturn(Optional.of(seoulParent));
    // when
    List<RegionDto> children = regionService.getRegions(name);
    // then
    assertNotNull(children);
    assertEquals(children.size(), 3);
    assertEquals(children.get(0).name(), "강남구");
    assertEquals(children.get(1).name(), "강서구");
    assertEquals(children.get(2).name(), "강동구");
  }

  @Test
  void 하위지역이_없으면_빈_배열을_반환한다() {
    // given
    String name = "세종특별자치시";
    when(regionRepository.findByName(name)).thenReturn(Optional.of(sejongParent));
    // when
    List<RegionDto> children = regionService.getRegions(name);
    // then
    assertNotNull(children);
    assertEquals(children.size(), 0);
  }

  @Test
  void 잘못된_상위지역을_입력하면_예외가_발생한다() {
    // given
    String name = "서울";
    when(regionRepository.findByName(name)).thenReturn(Optional.empty());
    // when
    CustomException e = assertThrows(CustomException.class,
        () -> regionService.getRegions(name));
    // then
    assertEquals(e.getErrorCode(), CITY_NOT_FOUND);
    assertEquals(e.getMessage(), "도시를 찾을 수 없습니다.");
  }

  @Test
  void 하위지역의_이름이_저장된_데이터와_일치하지_않으면_예외가_발생한다() {
    // given
    String city = "서울특별시";
    String district = "강북구 ";

    when(regionRepository.findByName(city)).thenReturn(Optional.of(seoulParent));
    when(regionRepository.findByNameAndParentId(district, seoulParent.getId())).thenReturn(
        Optional.empty());
    // when
    CustomException e = assertThrows(CustomException.class,
        () -> regionService.getChildRegionId(city, district));
    // then
    assertEquals(e.getErrorCode(), DISTRICT_NOT_FOUND);
    assertEquals(e.getMessage(), "지역을 찾을 수 없습니다.");
  }

  @Test
  void 상위지역의_아이디가_저장된_데이터와_일치하지_않으면_예외가_발생한다() {
    // given
    String city = "서울특별시";
    String district = "강북구";
    // when
    CustomException e = assertThrows(CustomException.class,
        () -> regionService.getChildRegionId(city, district));
    // then
    assertEquals(e.getErrorCode(), CITY_NOT_FOUND);
    assertEquals(e.getMessage(), "도시를 찾을 수 없습니다.");
  }

  @Test
  void 하위지역의_아이디로_상위지역의_이름과_하위지역의_이름을_반환한다() {
    // given
    Long regionId = 2L;
    when(regionRepository.findById(regionId)).thenReturn(Optional.of(seoulChild1));
    // when
    RegionResponseDto region = regionService.findById(regionId);
    // then
    assertEquals(regionId, seoulChild1.getId());
    assertEquals(region.district(), seoulChild1.getName());
    assertEquals(region.city(), seoulChild1.getParent().getName());
  }
}