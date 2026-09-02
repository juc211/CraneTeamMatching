package io.github.juc211.band_schedule.repository;

import io.github.juc211.band_schedule.domain.Club;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubRepository extends JpaRepository<Club, Long> {

	Optional<Club> findByAdminToken(String adminToken);
}
