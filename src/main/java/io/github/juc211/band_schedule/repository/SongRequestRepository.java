package io.github.juc211.band_schedule.repository;

import io.github.juc211.band_schedule.domain.SongRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SongRequestRepository extends JpaRepository<SongRequest, Long> {
}
