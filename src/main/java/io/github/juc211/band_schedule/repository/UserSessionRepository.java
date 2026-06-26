package io.github.juc211.band_schedule.repository;

import io.github.juc211.band_schedule.domain.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    List<UserSession> findByUserId(Long userId);
}
