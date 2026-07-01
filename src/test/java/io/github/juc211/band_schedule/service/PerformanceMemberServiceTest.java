package io.github.juc211.band_schedule.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.juc211.band_schedule.domain.Performance;
import io.github.juc211.band_schedule.domain.PerformanceMember;
import io.github.juc211.band_schedule.domain.User;
import io.github.juc211.band_schedule.dto.PerformanceDto;
import io.github.juc211.band_schedule.repository.PerformanceMemberRepository;
import io.github.juc211.band_schedule.repository.PerformanceRepository;
import io.github.juc211.band_schedule.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class PerformanceMemberServiceTest {

	@Autowired
	private PerformanceService performanceService;

	@Autowired
	private PerformanceRepository performanceRepository;

	@Autowired
	private PerformanceMemberRepository performanceMemberRepository;

	@Autowired
	private UserRepository userRepository;

	@Test
	void addPerformanceMembersPersistsSelectedUsersAsPerformanceMembers() {
		Performance performance = performanceRepository.save(
				Performance.create("2026 Summer Concert", LocalDate.of(2026, 8, 15), "Main Hall")
		);
		User vocal = userRepository.save(User.create("Kim Vocal", "20261234"));
		User bass = userRepository.save(User.create("Lee Bass", "20261235"));

		PerformanceDto.PerformanceMemberAddResponse response = performanceService.addPerformanceMembers(
				performance.getId(),
				new PerformanceDto.PerformanceMemberAddRequest(List.of(vocal.getId(), bass.getId()))
		);

		List<PerformanceMember> savedMembers = performanceMemberRepository.findAll();
		assertThat(savedMembers).hasSize(2);
		assertThat(savedMembers)
				.extracting(performanceMember -> performanceMember.getPerformance().getId())
				.containsOnly(performance.getId());
		assertThat(savedMembers)
				.extracting(performanceMember -> performanceMember.getUser().getId())
				.containsExactlyInAnyOrder(vocal.getId(), bass.getId());
		assertThat(response.performanceId()).isEqualTo(performance.getId());
		assertThat(response.members())
				.extracting(PerformanceDto.PerformanceMemberResponse::userId)
				.containsExactly(vocal.getId(), bass.getId());
	}
}
