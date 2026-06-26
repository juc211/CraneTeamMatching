package io.github.juc211.band_schedule.repository;

import io.github.juc211.band_schedule.domain.Availability;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvailabilityRepository extends JpaRepository<Availability, Long> {
}
