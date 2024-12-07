package com.gogym.region.controller;

import com.gogym.region.dto.RegionDto;
import com.gogym.region.service.RegionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/regions")
public class RegionController {

  private final RegionService regionService;

  // 입력 파라미터 값은 /api/regions?name={지역이름} 입니다.
  @GetMapping
  public ResponseEntity<List<RegionDto>> getRegions(@RequestParam String name) {

    List<RegionDto> districts = regionService.getRegions(name);

    return ResponseEntity.ok(districts);
  }
}