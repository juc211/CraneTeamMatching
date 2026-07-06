package io.github.juc211.band_schedule.repository;

import io.github.juc211.band_schedule.domain.Team;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {

	List<Team> findByPerformanceIdOrderByIdAsc(Long performanceId);

	void deleteByPerformanceId(Long performanceId);
}
