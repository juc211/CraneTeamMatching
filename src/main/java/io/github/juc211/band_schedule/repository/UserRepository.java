package io.github.juc211.band_schedule.repository;

import io.github.juc211.band_schedule.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Long> {
}
