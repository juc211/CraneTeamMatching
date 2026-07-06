package io.github.juc211.band_schedule.repository;

import io.github.juc211.band_schedule.domain.AvailableTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvailabilityRepository extends JpaRepository<AvailableTime, Long> {

	List<AvailableTime> findByTeamMemberIdOrderByStartDateTimeAscIdAsc(Long teamMemberId);

	List<AvailableTime> findByTeamMemberTeamIdOrderByStartDateTimeAscIdAsc(Long teamId);

	boolean existsByTeamMemberTeamPerformanceId(Long performanceId);

	void deleteByTeamMemberId(Long teamMemberId);

	void deleteByTeamMemberTeamId(Long teamId);

	void deleteByTeamMemberPerformanceMemberId(Long performanceMemberId);

	void deleteByTeamMemberTeamPerformanceId(Long performanceId);
}
