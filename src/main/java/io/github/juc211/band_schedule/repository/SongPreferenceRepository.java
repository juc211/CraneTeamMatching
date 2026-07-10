package io.github.juc211.band_schedule.repository;

import io.github.juc211.band_schedule.domain.SongPreference;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SongPreferenceRepository extends JpaRepository<SongPreference, Long> {

	List<SongPreference> findByPerformanceConfirmedSongPerformanceIdOrderByPerformanceConfirmedSongIdAscRankAscIdAsc(Long performanceId);

	List<SongPreference> findByPerformanceMemberIdAndPerformanceConfirmedSongPerformanceIdOrderByPerformanceConfirmedSongIdAsc(Long performanceMemberId, Long performanceId);

	void deleteByPerformanceConfirmedSongId(Long performanceConfirmedSongId);

	void deleteByPerformanceConfirmedSongPerformanceId(Long performanceId);

	void deleteByPerformanceMemberId(Long performanceMemberId);

	void deleteByPerformanceMemberIdAndPerformanceConfirmedSongPerformanceId(Long performanceMemberId, Long performanceId);
}
