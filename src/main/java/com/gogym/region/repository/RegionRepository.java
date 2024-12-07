package com.gogym.region.repository;

import com.gogym.region.entity.Region;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionRepository extends JpaRepository<Region, Long> {

  Optional<Region> findByName(String name);

  Optional<Region> findByNameAndParentId(String district, Long id);
}