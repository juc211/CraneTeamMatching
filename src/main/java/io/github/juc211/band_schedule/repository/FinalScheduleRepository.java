package io.github.juc211.band_schedule.repository;

import io.github.juc211.band_schedule.domain.FinalSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FinalScheduleRepository extends JpaRepository<FinalSchedule, Long> {
}
