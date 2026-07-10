package io.github.juc211.band_schedule.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.juc211.band_schedule.domain.AvailableTime;
import io.github.juc211.band_schedule.domain.InputLink;
import io.github.juc211.band_schedule.domain.InputLinkType;
import io.github.juc211.band_schedule.domain.Part;
import io.github.juc211.band_schedule.domain.Performance;
import io.github.juc211.band_schedule.domain.PerformanceMember;
import io.github.juc211.band_schedule.domain.Team;
import io.github.juc211.band_schedule.domain.TeamMember;
import io.github.juc211.band_schedule.domain.User;
import io.github.juc211.band_schedule.dto.AvailableTimeDto;
import io.github.juc211.band_schedule.repository.AvailabilityRepository;
import io.github.juc211.band_schedule.repository.InputLinkRepository;
import io.github.juc211.band_schedule.repository.PerformanceMemberRepository;
import io.github.juc211.band_schedule.repository.PerformanceRepository;
import io.github.juc211.band_schedule.repository.TeamMemberRepository;
import io.github.juc211.band_schedule.repository.TeamRepository;
import io.github.juc211.band_schedule.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class AvailableTimeServiceTest {

	@Autowired
	private AvailableTimeService availableTimeService;

	@Autowired
	private AvailabilityRepository availabilityRepository;

	@Autowired
	private PerformanceRepository performanceRepository;

	@Autowired
	private TeamRepository teamRepository;

	@Autowired
	private TeamMemberRepository teamMemberRepository;

	@Autowired
	private PerformanceMemberRepository performanceMemberRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private InputLinkRepository inputLinkRepository;

	@Test
	void replaceAvailableTimesByTeamMemberCreatesAvailableTimesWhenEmpty() {
		TeamMember teamMember = createTeamMemberWithScheduleWindow();

		List<AvailableTimeDto.AvailableTimeResponse> responses = availableTimeService.replaceAvailableTimesByTeamMember(
				teamMember.getId(),
				new AvailableTimeDto.AvailableTimesReplaceRequest(
						List.of(new AvailableTimeDto.AvailableTimeRequest(
								LocalDateTime.of(2026, 8, 1, 15, 0),
								LocalDateTime.of(2026, 8, 1, 18, 0)
						))
				)
		);

		AvailableTimeDto.AvailableTimeResponse response = responses.get(0);
		AvailableTime savedAvailableTime = availabilityRepository.findById(response.availableTimeId()).orElseThrow();
		assertThat(responses).hasSize(1);
		assertThat(response.teamMemberId()).isEqualTo(teamMember.getId());
		assertThat(response.name()).isEqualTo("Kim Vocal");
		assertThat(savedAvailableTime.getStartDateTime()).isEqualTo(LocalDateTime.of(2026, 8, 1, 15, 0));
		assertThat(savedAvailableTime.getEndDateTime()).isEqualTo(LocalDateTime.of(2026, 8, 1, 18, 0));
	}

	@Test
	void replaceAvailableTimesByTeamMemberReplacesExistingAvailableTimes() {
		TeamMember teamMember = createTeamMemberWithScheduleWindow();
		AvailableTime existingAvailableTime = availabilityRepository.save(AvailableTime.create(
				teamMember,
				LocalDateTime.of(2026, 8, 1, 15, 0),
				LocalDateTime.of(2026, 8, 1, 18, 0)
		));

		List<AvailableTimeDto.AvailableTimeResponse> responses = availableTimeService.replaceAvailableTimesByTeamMember(
				teamMember.getId(),
				new AvailableTimeDto.AvailableTimesReplaceRequest(
						List.of(
								new AvailableTimeDto.AvailableTimeRequest(
										LocalDateTime.of(2026, 8, 2, 16, 0),
										LocalDateTime.of(2026, 8, 2, 20, 0)
								),
								new AvailableTimeDto.AvailableTimeRequest(
										LocalDateTime.of(2026, 8, 3, 18, 0),
										LocalDateTime.of(2026, 8, 3, 22, 0)
								)
						)
				)
		);

		assertThat(availabilityRepository.findById(existingAvailableTime.getId())).isEmpty();
		assertThat(responses)
				.extracting(AvailableTimeDto.AvailableTimeResponse::startDateTime)
				.containsExactly(
						LocalDateTime.of(2026, 8, 2, 16, 0),
						LocalDateTime.of(2026, 8, 3, 18, 0)
				);
	}

	@Test
	void replaceAvailableTimesByTeamMemberClearsAvailableTimesWhenEmptyListIsSubmitted() {
		TeamMember teamMember = createTeamMemberWithScheduleWindow();
		availabilityRepository.save(AvailableTime.create(
				teamMember,
				LocalDateTime.of(2026, 8, 1, 15, 0),
				LocalDateTime.of(2026, 8, 1, 18, 0)
		));

		List<AvailableTimeDto.AvailableTimeResponse> responses = availableTimeService.replaceAvailableTimesByTeamMember(
				teamMember.getId(),
				new AvailableTimeDto.AvailableTimesReplaceRequest(List.of())
		);

		assertThat(responses).isEmpty();
		assertThat(availabilityRepository.findByTeamMemberIdOrderByStartDateTimeAscIdAsc(teamMember.getId())).isEmpty();
	}

	@Test
	void replaceAvailableTimesByTeamMemberWithLinkCreatesAvailableTimes() {
		TeamMember teamMember = createTeamMemberWithScheduleWindow();
		inputLinkRepository.save(InputLink.create(
				"available-token",
				teamMember.getTeam().getPerformance(),
				InputLinkType.AVAILABLE_TIME,
				true,
				null
		));

		List<AvailableTimeDto.AvailableTimeResponse> responses = availableTimeService.replaceAvailableTimesByTeamMember(
				"available-token",
				teamMember.getId(),
				new AvailableTimeDto.AvailableTimesReplaceRequest(
						List.of(new AvailableTimeDto.AvailableTimeRequest(
								LocalDateTime.of(2026, 8, 1, 15, 0),
								LocalDateTime.of(2026, 8, 1, 18, 0)
						))
				)
		);

		assertThat(responses).singleElement()
				.satisfies(response -> {
					assertThat(response.teamMemberId()).isEqualTo(teamMember.getId());
					assertThat(response.startDateTime()).isEqualTo(LocalDateTime.of(2026, 8, 1, 15, 0));
				});
	}

	@Test
	void replaceAvailableTimesByTeamMemberWithLinkRejectsWrongLinkType() {
		TeamMember teamMember = createTeamMemberWithScheduleWindow();
		inputLinkRepository.save(InputLink.create(
				"view-token",
				teamMember.getTeam().getPerformance(),
				InputLinkType.FINAL_SCHEDULE_VIEW,
				true,
				null
		));

		assertThatThrownBy(() -> availableTimeService.replaceAvailableTimesByTeamMember(
				"view-token",
				teamMember.getId(),
				new AvailableTimeDto.AvailableTimesReplaceRequest(List.of())
		))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("InputLink type must be AVAILABLE_TIME");
	}

	@Test
	void replaceAvailableTimesByTeamMemberWithLinkRejectsTeamMemberFromDifferentPerformance() {
		TeamMember teamMember = createTeamMemberWithScheduleWindow();
		TeamMember otherTeamMember = createTeamMemberWithScheduleWindow();
		inputLinkRepository.save(InputLink.create(
				"available-token",
				teamMember.getTeam().getPerformance(),
				InputLinkType.AVAILABLE_TIME,
				true,
				null
		));

		assertThatThrownBy(() -> availableTimeService.replaceAvailableTimesByTeamMember(
				"available-token",
				otherTeamMember.getId(),
				new AvailableTimeDto.AvailableTimesReplaceRequest(List.of())
		))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("TeamMember does not belong to link performance");
	}

	@Test
	void replaceAvailableTimesByTeamMemberRejectsPerformanceWithoutScheduleWindow() {
		TeamMember teamMember = createTeamMemberWithoutScheduleWindow();

		assertThatThrownBy(() -> availableTimeService.replaceAvailableTimesByTeamMember(
				teamMember.getId(),
				new AvailableTimeDto.AvailableTimesReplaceRequest(
						List.of(new AvailableTimeDto.AvailableTimeRequest(
								LocalDateTime.of(2026, 8, 1, 15, 0),
								LocalDateTime.of(2026, 8, 1, 18, 0)
						))
				)
		))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Performance schedule window is required");
	}

	@Test
	void replaceAvailableTimesByTeamMemberRejectsTimeOutsideScheduleWindow() {
		TeamMember teamMember = createTeamMemberWithScheduleWindow();

		assertThatThrownBy(() -> availableTimeService.replaceAvailableTimesByTeamMember(
				teamMember.getId(),
				new AvailableTimeDto.AvailableTimesReplaceRequest(
						List.of(new AvailableTimeDto.AvailableTimeRequest(
								LocalDateTime.of(2026, 7, 31, 15, 0),
								LocalDateTime.of(2026, 8, 1, 18, 0)
						))
				)
		))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Available time must be within performance schedule window");
	}

	@Test
	void replaceAvailableTimesByTeamMemberAllowsEndAtNextDayMidnightOfScheduleWindowEndDate() {
		Performance performance = performanceRepository.save(Performance.create(
				"2026 Summer Concert",
				LocalDate.of(2026, 8, 20),
				"Main Hall",
				LocalDate.of(2026, 8, 1),
				LocalDate.of(2026, 8, 19)
		));
		TeamMember teamMember = createTeamMember(performance);

		List<AvailableTimeDto.AvailableTimeResponse> responses = availableTimeService.replaceAvailableTimesByTeamMember(
				teamMember.getId(),
				new AvailableTimeDto.AvailableTimesReplaceRequest(
						List.of(new AvailableTimeDto.AvailableTimeRequest(
								LocalDateTime.of(2026, 8, 19, 23, 0),
								LocalDateTime.of(2026, 8, 20, 0, 0)
						))
				)
		);

		assertThat(responses).singleElement()
				.satisfies(response -> {
					assertThat(response.startDateTime()).isEqualTo(LocalDateTime.of(2026, 8, 19, 23, 0));
					assertThat(response.endDateTime()).isEqualTo(LocalDateTime.of(2026, 8, 20, 0, 0));
				});
	}

	@Test
	void getAvailableTimesByTeamMemberReturnsAvailableTimesInOrder() {
		TeamMember teamMember = createTeamMemberWithScheduleWindow();
		availabilityRepository.save(AvailableTime.create(
				teamMember,
				LocalDateTime.of(2026, 8, 2, 16, 0),
				LocalDateTime.of(2026, 8, 2, 20, 0)
		));
		availabilityRepository.save(AvailableTime.create(
				teamMember,
				LocalDateTime.of(2026, 8, 1, 15, 0),
				LocalDateTime.of(2026, 8, 1, 18, 0)
		));

		assertThat(availableTimeService.getAvailableTimesByTeamMember(teamMember.getId()))
				.extracting(AvailableTimeDto.AvailableTimeResponse::startDateTime)
				.containsExactly(
						LocalDateTime.of(2026, 8, 1, 15, 0),
						LocalDateTime.of(2026, 8, 2, 16, 0)
				);
	}

	@Test
	void getAvailableTimesByTeamReturnsTeamAvailableTimes() {
		TeamMember teamMember = createTeamMemberWithScheduleWindow();
		availabilityRepository.save(AvailableTime.create(
				teamMember,
				LocalDateTime.of(2026, 8, 1, 15, 0),
				LocalDateTime.of(2026, 8, 1, 18, 0)
		));

		assertThat(availableTimeService.getAvailableTimesByTeam(teamMember.getTeam().getId()))
				.singleElement()
				.satisfies(response -> {
					assertThat(response.teamMemberId()).isEqualTo(teamMember.getId());
					assertThat(response.teamId()).isEqualTo(teamMember.getTeam().getId());
				});
	}

	@Test
	void getAvailableTimeOverlapsByTeamReturnsCommonAvailableTimesForAllTeamMembers() {
		Performance performance = performanceRepository.save(Performance.create(
				"2026 Summer Concert",
				LocalDate.of(2026, 8, 20),
				"Main Hall",
				LocalDate.of(2026, 8, 1),
				LocalDate.of(2026, 8, 20)
		));
		Team team = teamRepository.save(Team.create(performance, "Team A", "Song A"));
		TeamMember vocal = createTeamMember(performance, team, "Kim Vocal", "20261234", Part.VOCAL);
		TeamMember guitar = createTeamMember(performance, team, "Choi Guitar", "20261235", Part.GUITAR);
		TeamMember drum = createTeamMember(performance, team, "Park Drum", "20261236", Part.DRUM);

		availabilityRepository.save(AvailableTime.create(
				vocal,
				LocalDateTime.of(2026, 8, 1, 15, 0),
				LocalDateTime.of(2026, 8, 1, 18, 0)
		));
		availabilityRepository.save(AvailableTime.create(
				guitar,
				LocalDateTime.of(2026, 8, 1, 16, 0),
				LocalDateTime.of(2026, 8, 1, 19, 0)
		));
		availabilityRepository.save(AvailableTime.create(
				drum,
				LocalDateTime.of(2026, 8, 1, 14, 0),
				LocalDateTime.of(2026, 8, 1, 17, 0)
		));

		assertThat(availableTimeService.getAvailableTimeOverlapsByTeam(team.getId()))
				.singleElement()
				.satisfies(response -> {
					assertThat(response.teamId()).isEqualTo(team.getId());
					assertThat(response.requiredTeamMemberCount()).isEqualTo(3);
					assertThat(response.availableTeamMemberCount()).isEqualTo(3);
					assertThat(response.startDateTime()).isEqualTo(LocalDateTime.of(2026, 8, 1, 16, 0));
					assertThat(response.endDateTime()).isEqualTo(LocalDateTime.of(2026, 8, 1, 17, 0));
				});
	}

	@Test
	void getAvailableTimeOverlapsByTeamReturnsEmptyListWhenAnyTeamMemberHasNoAvailableTime() {
		Performance performance = performanceRepository.save(Performance.create(
				"2026 Summer Concert",
				LocalDate.of(2026, 8, 20),
				"Main Hall",
				LocalDate.of(2026, 8, 1),
				LocalDate.of(2026, 8, 20)
		));
		Team team = teamRepository.save(Team.create(performance, "Team A", "Song A"));
		TeamMember vocal = createTeamMember(performance, team, "Kim Vocal", "20261234", Part.VOCAL);
		createTeamMember(performance, team, "Choi Guitar", "20261235", Part.GUITAR);
		availabilityRepository.save(AvailableTime.create(
				vocal,
				LocalDateTime.of(2026, 8, 1, 15, 0),
				LocalDateTime.of(2026, 8, 1, 18, 0)
		));

		assertThat(availableTimeService.getAvailableTimeOverlapsByTeam(team.getId())).isEmpty();
	}

	private TeamMember createTeamMemberWithScheduleWindow() {
		Performance performance = performanceRepository.save(Performance.create(
				"2026 Summer Concert",
				LocalDate.of(2026, 8, 20),
				"Main Hall",
				LocalDate.of(2026, 8, 1),
				LocalDate.of(2026, 8, 20)
		));
		return createTeamMember(performance);
	}

	private TeamMember createTeamMemberWithoutScheduleWindow() {
		Performance performance = performanceRepository.save(Performance.create(
				"2026 Summer Concert",
				LocalDate.of(2026, 8, 20),
				"Main Hall"
		));
		return createTeamMember(performance);
	}

	private TeamMember createTeamMember(Performance performance) {
		Team team = teamRepository.save(Team.create(performance, "Team A", "Song A"));
		return createTeamMember(performance, team, "Kim Vocal", "20261234", Part.VOCAL);
	}

	private TeamMember createTeamMember(Performance performance, Team team, String name, String studentNumber, Part part) {
		User user = userRepository.save(User.create(name, studentNumber));
		PerformanceMember performanceMember = performanceMemberRepository.save(PerformanceMember.create(performance, user));
		return teamMemberRepository.save(TeamMember.create(team, performanceMember, part));
	}
}
