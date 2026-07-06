package io.github.juc211.band_schedule.repository;

import io.github.juc211.band_schedule.domain.SongRequest;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SongRequestRepository extends JpaRepository<SongRequest, Long> {

	List<SongRequest> findByPerformanceIdOrderByIdAsc(Long performanceId);

	List<SongRequest> findByTeamIdOrderByIdAsc(Long teamId);

	void deleteByTeamId(Long teamId);

	void deleteByPerformanceId(Long performanceId);

	void deleteByRequestedByMemberId(Long performanceMemberId);
}
