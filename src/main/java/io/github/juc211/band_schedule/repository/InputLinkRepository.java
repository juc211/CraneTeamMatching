package io.github.juc211.band_schedule.repository;

import io.github.juc211.band_schedule.domain.InputLink;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InputLinkRepository extends JpaRepository<InputLink, Long> {

	Optional<InputLink> findByToken(String token);
}
