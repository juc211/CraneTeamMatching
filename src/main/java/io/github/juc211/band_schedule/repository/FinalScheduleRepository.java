package io.github.juc211.band_schedule.repository;

import io.github.juc211.band_schedule.domain.FinalSchedule;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FinalScheduleRepository extends JpaRepository<FinalSchedule, Long> {

	boolean existsByTeamPerformanceId(Long performanceId);

	List<FinalSchedule> findByTeamIdOrderByStartDateTimeAscIdAsc(Long teamId);

	List<FinalSchedule> findByTeamPerformanceIdOrderByStartDateTimeAscIdAsc(Long performanceId);

	List<FinalSchedule> findByStartDateTimeLessThanAndEndDateTimeGreaterThan(
			LocalDateTime endDateTime,
			LocalDateTime startDateTime
	);

	void deleteByTeamId(Long teamId);

	void deleteByTeamPerformanceId(Long performanceId);
}
