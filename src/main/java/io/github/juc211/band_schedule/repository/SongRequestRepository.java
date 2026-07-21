package io.github.juc211.band_schedule.repository;

import io.github.juc211.band_schedule.domain.SongRequest;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SongRequestRepository extends JpaRepository<SongRequest, Long> {

	@Query("select sr from SongRequest sr " +
			"join fetch sr.performance p " +
			"left join fetch sr.team t " +
			"join fetch sr.requestedByMember rm " +
			"where p.id = :performanceId order by sr.id asc")
	List<SongRequest> findByPerformanceIdOrderByIdAsc(@Param("performanceId") Long performanceId);

	@Query("select sr from SongRequest sr " +
			"join fetch sr.performance p " +
			"join fetch sr.team t " +
			"join fetch sr.requestedByMember rm " +
			"where t.id = :teamId order by sr.id asc")
	List<SongRequest> findByTeamIdOrderByIdAsc(@Param("teamId") Long teamId);

	void deleteByTeamId(Long teamId);

	void deleteByPerformanceId(Long performanceId);

	void deleteByRequestedByMemberId(Long performanceMemberId);
}
