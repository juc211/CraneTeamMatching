package io.github.juc211.band_schedule.repository;

import io.github.juc211.band_schedule.domain.AvailableTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvailabilityRepository extends JpaRepository<AvailableTime, Long> {
}
