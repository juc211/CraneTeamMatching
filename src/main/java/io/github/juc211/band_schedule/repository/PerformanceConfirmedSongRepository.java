package io.github.juc211.band_schedule.repository;

import io.github.juc211.band_schedule.domain.PerformanceConfirmedSong;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerformanceConfirmedSongRepository extends JpaRepository<PerformanceConfirmedSong, Long> {

	List<PerformanceConfirmedSong> findByPerformanceIdOrderByIdAsc(Long performanceId);

	void deleteByPerformanceId(Long performanceId);
}
