package io.github.juc211.band_schedule.repository;

import io.github.juc211.band_schedule.domain.SongVote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SongVoteRepository extends JpaRepository<SongVote, Long> {
}
