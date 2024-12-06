package com.gogym.region.dto;

import com.gogym.region.entity.Region;

public record RegionDto(

    Long regionId,
    String name
) {
    public static RegionDto fromEntity(Region region) {
      return new RegionDto(region.getId(), region.getName());
    }
}
