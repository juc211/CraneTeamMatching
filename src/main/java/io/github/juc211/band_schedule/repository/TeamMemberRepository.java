package io.github.juc211.band_schedule.repository;

import io.github.juc211.band_schedule.domain.TeamMember;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

	// join fetch추가 - N+1문제 해결
	@Query("select tm from TeamMember tm " +
			"join fetch tm.team t " +
			"join fetch tm.performanceMember pm " +
			"join fetch pm.user u " +
			"where t.id = :teamId order by tm.id asc")
	List<TeamMember> findByTeamIdOrderByIdAsc(@Param("teamId") Long teamId);

	List<TeamMember> findByPerformanceMemberIdOrderByIdAsc(Long performanceMemberId);

	void deleteByTeamId(Long teamId);

	void deleteByPerformanceMemberId(Long performanceMemberId);

	void deleteByTeamPerformanceId(Long performanceId);
}
