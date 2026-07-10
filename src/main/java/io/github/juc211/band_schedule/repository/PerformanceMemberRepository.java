package io.github.juc211.band_schedule.repository;

import io.github.juc211.band_schedule.domain.PerformanceMember;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerformanceMemberRepository extends JpaRepository<PerformanceMember, Long> {

	List<PerformanceMember> findByPerformanceIdOrderByIdAsc(Long performanceId);

	Optional<PerformanceMember> findByPerformanceIdAndUserNameAndUserStudentNumber(Long performanceId, String name, String studentNumber);

	boolean existsByPerformanceIdAndUserId(Long performanceId, Long userId);

	boolean existsByUserId(Long userId);

	void deleteByPerformanceId(Long performanceId);
}
