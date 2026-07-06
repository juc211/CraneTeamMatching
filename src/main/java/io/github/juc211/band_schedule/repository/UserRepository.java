package io.github.juc211.band_schedule.repository;

import io.github.juc211.band_schedule.domain.User;
import io.github.juc211.band_schedule.domain.UserStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Long> {

	List<User> findByStatusOrderByIdAsc(UserStatus status);
}
