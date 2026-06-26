package io.github.juc211.band_schedule.repository;

import io.github.juc211.band_schedule.domain.Performance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerformanceRepository extends JpaRepository<Performance, Long> {
}
