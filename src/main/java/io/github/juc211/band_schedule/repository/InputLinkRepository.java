package io.github.juc211.band_schedule.repository;

import io.github.juc211.band_schedule.domain.InputLink;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InputLinkRepository extends JpaRepository<InputLink, Long> {

	Optional<InputLink> findByToken(String token);

	List<InputLink> findByPerformanceIdOrderByIdAsc(Long performanceId);

	boolean existsByToken(String token);

	void deleteByPerformanceId(Long performanceId);
}
