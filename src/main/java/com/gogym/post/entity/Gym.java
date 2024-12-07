package com.gogym.post.entity;

import com.gogym.common.entity.BaseIdEntity;
import com.gogym.post.dto.PostRequestDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Gym extends BaseIdEntity {

  @Column(name = "gym_name", nullable = false)
  private String gymName;

  @Column(nullable = false)
  private Double latitude;

  @Column(nullable = false)
  private Double longitude;

  @Column(name = "gym_kakao_url", nullable = false)
  private String gymKakaoUrl;

  @Column(nullable = false)
  private Long regionId;

  public static Gym createGym(PostRequestDto postRequestDto, Long regionId) {

    return Gym.builder()
        .gymName(postRequestDto.gymName())
        .latitude(postRequestDto.latitude())
        .longitude(postRequestDto.longitude())
        .gymKakaoUrl(postRequestDto.gymKakaoUrl())
        .regionId(regionId)
        .build();
  }
}