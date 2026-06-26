package io.github.juc211.band_schedule.repository;

import io.github.juc211.band_schedule.domain.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
}
