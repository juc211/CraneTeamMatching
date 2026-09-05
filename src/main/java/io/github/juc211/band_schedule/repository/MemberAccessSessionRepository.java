package io.github.juc211.band_schedule.repository;

import io.github.juc211.band_schedule.domain.MemberAccessSession;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberAccessSessionRepository extends JpaRepository<MemberAccessSession, Long> {

	Optional<MemberAccessSession> findByToken(String token);

	boolean existsByToken(String token);

	void deleteByInputLinkId(Long inputLinkId);

	void deleteByInputLinkPerformanceId(Long performanceId);

	void deleteByPerformanceMemberId(Long performanceMemberId);
}
