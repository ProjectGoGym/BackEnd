package com.gogym.post.repository;

import com.gogym.post.entity.Gym;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GymRepository extends JpaRepository<Gym, Long> {

  Optional<Gym> findByLatitudeAndLongitude(Double latitude, Double longitude);
}