package io.github.juc211.band_schedule.repository;

import io.github.juc211.band_schedule.domain.SongVote;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SongVoteRepository extends JpaRepository<SongVote, Long> {

	Optional<SongVote> findBySongRequestIdAndVoterMemberId(Long songRequestId, Long voterMemberId);

	@EntityGraph(attributePaths = {"songRequest", "voterMember"})
	List<SongVote> findBySongRequestIdOrderByIdAsc(Long songRequestId);

	void deleteBySongRequestId(Long songRequestId);

	void deleteBySongRequestTeamId(Long teamId);

	void deleteBySongRequestPerformanceId(Long performanceId);

	void deleteBySongRequestRequestedByMemberId(Long performanceMemberId);

	void deleteByVoterMemberId(Long performanceMemberId);
}
