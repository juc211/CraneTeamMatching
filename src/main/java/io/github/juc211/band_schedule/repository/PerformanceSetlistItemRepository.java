package io.github.juc211.band_schedule.repository;

import io.github.juc211.band_schedule.domain.PerformanceSetlistItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerformanceSetlistItemRepository extends JpaRepository<PerformanceSetlistItem, Long> {

	List<PerformanceSetlistItem> findByPerformanceIdOrderBySequenceNumberAscIdAsc(Long performanceId);

	void deleteByPerformanceId(Long performanceId);

	void deleteByTeamId(Long teamId);
}
