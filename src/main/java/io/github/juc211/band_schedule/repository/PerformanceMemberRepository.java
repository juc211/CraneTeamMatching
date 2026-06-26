package io.github.juc211.band_schedule.repository;

import io.github.juc211.band_schedule.domain.PerformanceMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerformanceMemberRepository extends JpaRepository<PerformanceMember, Long> {
}
