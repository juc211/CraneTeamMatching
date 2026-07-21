package io.github.juc211.band_schedule.repository;

import io.github.juc211.band_schedule.domain.Team;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {

	@EntityGraph(attributePaths = {"performance"}) //팀 데이터를 가져올 때 공연(performance)데이터까지 한번에 가져와서 N+1문제 해결
	List<Team> findByPerformanceIdOrderByIdAsc(Long performanceId);

	void deleteByPerformanceId(Long performanceId);
}
