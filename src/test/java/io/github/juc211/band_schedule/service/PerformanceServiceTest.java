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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class PerformanceServiceTest {

	@Autowired
	private PerformanceService performanceService;

	@Autowired
	private PerformanceRepository performanceRepository;

	@Autowired
	private PerformanceMemberRepository performanceMemberRepository;

	@Autowired
	private UserRepository userRepository;

	@Test
	void createPerformancePersistsPerformanceAndReturnsResponse() {
		PerformanceDto.PerformanceCreateRequest request = new PerformanceDto.PerformanceCreateRequest(
				"2026 Summer Concert",
				LocalDate.of(2026, 8, 15),
				"Main Hall"
		);

		PerformanceDto.PerformanceCreateResponse response = performanceService.createPerformance(request);

		Performance savedPerformance = performanceRepository.findById(response.performanceId()).orElseThrow();
		assertThat(savedPerformance.getTitle()).isEqualTo("2026 Summer Concert");
		assertThat(savedPerformance.getPerformanceDate()).isEqualTo(LocalDate.of(2026, 8, 15));
		assertThat(savedPerformance.getLocation()).isEqualTo("Main Hall");
		assertThat(savedPerformance.getCreatedAt()).isNotNull();
		assertThat(response.title()).isEqualTo("2026 Summer Concert");
	}

	@Test
	void getPerformancesReturnsRegisteredPerformances() {
		performanceRepository.save(Performance.create(
				"2026 Summer Concert",
				LocalDate.of(2026, 8, 15),
				"Main Hall"
		));
		performanceRepository.save(Performance.create(
				"2026 Winter Concert",
				LocalDate.of(2026, 12, 20),
				"Club Room"
		));

		assertThat(performanceService.getPerformances())
				.extracting(PerformanceDto.PerformanceResponse::title)
				.containsExactly("2026 Summer Concert", "2026 Winter Concert");
	}

	@Test
	void getPerformanceReturnsSinglePerformance() {
		Performance performance = performanceRepository.save(Performance.create(
				"2026 Summer Concert",
				LocalDate.of(2026, 8, 15),
				"Main Hall"
		));

		PerformanceDto.PerformanceResponse response = performanceService.getPerformance(performance.getId());

		assertThat(response.performanceId()).isEqualTo(performance.getId());
		assertThat(response.title()).isEqualTo("2026 Summer Concert");
		assertThat(response.performanceDate()).isEqualTo(LocalDate.of(2026, 8, 15));
		assertThat(response.location()).isEqualTo("Main Hall");
	}

	@Test
	void updatePerformanceChangesPerformanceFields() {
		Performance performance = performanceRepository.save(Performance.create(
				"2026 Summer Concert",
				LocalDate.of(2026, 8, 15),
				"Main Hall"
		));

		PerformanceDto.PerformanceResponse response = performanceService.updatePerformance(
				performance.getId(),
				new PerformanceDto.PerformanceUpdateRequest(
						"2026 Winter Concert",
						LocalDate.of(2026, 12, 20),
						"Club Room"
				)
		);

		assertThat(response.performanceId()).isEqualTo(performance.getId());
		assertThat(response.title()).isEqualTo("2026 Winter Concert");
		assertThat(response.performanceDate()).isEqualTo(LocalDate.of(2026, 12, 20));
		assertThat(response.location()).isEqualTo("Club Room");
		assertThat(performanceRepository.findById(performance.getId()).orElseThrow().getTitle())
				.isEqualTo("2026 Winter Concert");
	}

	@Test
	void getPerformanceMembersReturnsMembersInPerformance() {
		Performance performance = performanceRepository.save(Performance.create(
				"2026 Summer Concert",
				LocalDate.of(2026, 8, 15),
				"Main Hall"
		));
		User user = userRepository.save(User.create("Kim Band", "20261234"));
		PerformanceMember performanceMember = performanceMemberRepository.save(PerformanceMember.create(performance, user));

		assertThat(performanceService.getPerformanceMembers(performance.getId()))
				.singleElement()
				.satisfies(response -> {
					assertThat(response.performanceMemberId()).isEqualTo(performanceMember.getId());
					assertThat(response.userId()).isEqualTo(user.getId());
					assertThat(response.name()).isEqualTo("Kim Band");
				});
	}
}
