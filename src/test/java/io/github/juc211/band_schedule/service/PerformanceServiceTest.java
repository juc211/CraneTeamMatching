package io.github.juc211.band_schedule.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.juc211.band_schedule.domain.AvailableTime;
import io.github.juc211.band_schedule.domain.FinalSchedule;
import io.github.juc211.band_schedule.domain.InputLink;
import io.github.juc211.band_schedule.domain.InputLinkType;
import io.github.juc211.band_schedule.domain.Part;
import io.github.juc211.band_schedule.domain.Performance;
import io.github.juc211.band_schedule.domain.PerformanceConfirmedSong;
import io.github.juc211.band_schedule.domain.PerformanceMember;
import io.github.juc211.band_schedule.domain.SongRequest;
import io.github.juc211.band_schedule.domain.SongVote;
import io.github.juc211.band_schedule.domain.Team;
import io.github.juc211.band_schedule.domain.TeamMember;
import io.github.juc211.band_schedule.domain.User;
import io.github.juc211.band_schedule.domain.Vote;
import io.github.juc211.band_schedule.dto.PerformanceDto;
import io.github.juc211.band_schedule.repository.AvailabilityRepository;
import io.github.juc211.band_schedule.repository.PerformanceMemberRepository;
import io.github.juc211.band_schedule.repository.FinalScheduleRepository;
import io.github.juc211.band_schedule.repository.InputLinkRepository;
import io.github.juc211.band_schedule.repository.PerformanceConfirmedSongRepository;
import io.github.juc211.band_schedule.repository.PerformanceRepository;
import io.github.juc211.band_schedule.repository.SongRequestRepository;
import io.github.juc211.band_schedule.repository.SongVoteRepository;
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
class PerformanceServiceTest {

	@Autowired
	private PerformanceService performanceService;

	@Autowired
	private PerformanceRepository performanceRepository;

	@Autowired
	private PerformanceMemberRepository performanceMemberRepository;

	@Autowired
	private TeamRepository teamRepository;

	@Autowired
	private TeamMemberRepository teamMemberRepository;

	@Autowired
	private AvailabilityRepository availabilityRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private FinalScheduleRepository finalScheduleRepository;

	@Autowired
	private InputLinkRepository inputLinkRepository;

	@Autowired
	private PerformanceConfirmedSongRepository performanceConfirmedSongRepository;

	@Autowired
	private SongRequestRepository songRequestRepository;

	@Autowired
	private SongVoteRepository songVoteRepository;

	@Test
	void createPerformancePersistsPerformanceAndReturnsResponse() {
		PerformanceDto.PerformanceCreateRequest request = new PerformanceDto.PerformanceCreateRequest(
				"2026 Summer Concert",
				LocalDate.of(2026, 8, 15),
				"Main Hall",
				LocalDate.of(2026, 8, 1),
				LocalDate.of(2026, 8, 14)
		);

		PerformanceDto.PerformanceCreateResponse response = performanceService.createPerformance(request);

		Performance savedPerformance = performanceRepository.findById(response.performanceId()).orElseThrow();
		assertThat(savedPerformance.getTitle()).isEqualTo("2026 Summer Concert");
		assertThat(savedPerformance.getPerformanceDate()).isEqualTo(LocalDate.of(2026, 8, 15));
		assertThat(savedPerformance.getLocation()).isEqualTo("Main Hall");
		assertThat(savedPerformance.getScheduleWindowStartDate()).isEqualTo(LocalDate.of(2026, 8, 1));
		assertThat(savedPerformance.getScheduleWindowEndDate()).isEqualTo(LocalDate.of(2026, 8, 14));
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
		assertThat(response.scheduleWindowStartDate()).isNull();
		assertThat(response.scheduleWindowEndDate()).isNull();
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
						"Club Room",
						LocalDate.of(2026, 12, 1),
						LocalDate.of(2026, 12, 19)
				)
		);

		assertThat(response.performanceId()).isEqualTo(performance.getId());
		assertThat(response.title()).isEqualTo("2026 Winter Concert");
		assertThat(response.performanceDate()).isEqualTo(LocalDate.of(2026, 12, 20));
		assertThat(response.location()).isEqualTo("Club Room");
		assertThat(response.scheduleWindowStartDate()).isEqualTo(LocalDate.of(2026, 12, 1));
		assertThat(response.scheduleWindowEndDate()).isEqualTo(LocalDate.of(2026, 12, 19));
		assertThat(performanceRepository.findById(performance.getId()).orElseThrow().getTitle())
				.isEqualTo("2026 Winter Concert");
	}

	@Test
	void updatePerformanceScheduleWindowChangesOnlyScheduleWindow() {
		Performance performance = performanceRepository.save(Performance.create(
				"2026 Summer Concert",
				LocalDate.of(2026, 8, 20),
				"Main Hall"
		));

		PerformanceDto.PerformanceResponse response = performanceService.updatePerformanceScheduleWindow(
				performance.getId(),
				new PerformanceDto.PerformanceScheduleWindowUpdateRequest(
						LocalDate.of(2026, 8, 1),
						LocalDate.of(2026, 8, 20)
				)
		);

		assertThat(response.performanceId()).isEqualTo(performance.getId());
		assertThat(response.title()).isEqualTo("2026 Summer Concert");
		assertThat(response.performanceDate()).isEqualTo(LocalDate.of(2026, 8, 20));
		assertThat(response.location()).isEqualTo("Main Hall");
		assertThat(response.scheduleWindowStartDate()).isEqualTo(LocalDate.of(2026, 8, 1));
		assertThat(response.scheduleWindowEndDate()).isEqualTo(LocalDate.of(2026, 8, 20));
	}

	@Test
	void addPerformanceMembersRejectsDuplicateUserInSameRequest() {
		Performance performance = performanceRepository.save(Performance.create(
				"2026 Summer Concert",
				LocalDate.of(2026, 8, 20),
				"Main Hall"
		));
		User user = userRepository.save(User.create("Kim Vocal", "20261234"));

		assertThatThrownBy(() -> performanceService.addPerformanceMembers(
				performance.getId(),
				new PerformanceDto.PerformanceMemberAddRequest(List.of(user.getId(), user.getId()))
		))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Duplicate user id");
	}

	@Test
	void addPerformanceMembersRejectsUserAlreadyAddedToPerformance() {
		Performance performance = performanceRepository.save(Performance.create(
				"2026 Summer Concert",
				LocalDate.of(2026, 8, 20),
				"Main Hall"
		));
		User user = userRepository.save(User.create("Kim Vocal", "20261234"));
		performanceMemberRepository.save(PerformanceMember.create(performance, user));

		assertThatThrownBy(() -> performanceService.addPerformanceMembers(
				performance.getId(),
				new PerformanceDto.PerformanceMemberAddRequest(List.of(user.getId()))
		))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("User is already added to performance");
	}

	@Test
	void getPerformanceScheduleWindowReturnsScheduleWindow() {
		Performance performance = performanceRepository.save(Performance.create(
				"2026 Summer Concert",
				LocalDate.of(2026, 8, 20),
				"Main Hall",
				LocalDate.of(2026, 8, 1),
				LocalDate.of(2026, 8, 20)
		));

		PerformanceDto.PerformanceScheduleWindowResponse response =
				performanceService.getPerformanceScheduleWindow(performance.getId());

		assertThat(response.performanceId()).isEqualTo(performance.getId());
		assertThat(response.scheduleWindowStartDate()).isEqualTo(LocalDate.of(2026, 8, 1));
		assertThat(response.scheduleWindowEndDate()).isEqualTo(LocalDate.of(2026, 8, 20));
	}

	@Test
	void deletePerformanceScheduleWindowClearsScheduleWindow() {
		Performance performance = performanceRepository.save(Performance.create(
				"2026 Summer Concert",
				LocalDate.of(2026, 8, 20),
				"Main Hall",
				LocalDate.of(2026, 8, 1),
				LocalDate.of(2026, 8, 20)
		));

		performanceService.deletePerformanceScheduleWindow(performance.getId());

		Performance savedPerformance = performanceRepository.findById(performance.getId()).orElseThrow();
		assertThat(savedPerformance.getTitle()).isEqualTo("2026 Summer Concert");
		assertThat(savedPerformance.getScheduleWindowStartDate()).isNull();
		assertThat(savedPerformance.getScheduleWindowEndDate()).isNull();
	}

	@Test
	void deletePerformanceScheduleWindowRejectsWhenAvailableTimesExist() {
		Performance performance = performanceRepository.save(Performance.create(
				"2026 Summer Concert",
				LocalDate.of(2026, 8, 20),
				"Main Hall",
				LocalDate.of(2026, 8, 1),
				LocalDate.of(2026, 8, 20)
		));
		User user = userRepository.save(User.create("Kim Vocal", "20261234"));
		PerformanceMember performanceMember = performanceMemberRepository.save(PerformanceMember.create(performance, user));
		Team team = teamRepository.save(Team.create(performance, "Team A", "Song A"));
		TeamMember teamMember = teamMemberRepository.save(TeamMember.create(team, performanceMember, Part.VOCAL));
		availabilityRepository.save(AvailableTime.create(
				teamMember,
				LocalDateTime.of(2026, 8, 1, 15, 0),
				LocalDateTime.of(2026, 8, 1, 18, 0)
		));

		assertThatThrownBy(() -> performanceService.deletePerformanceScheduleWindow(performance.getId()))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Cannot delete schedule window because available times exist");
	}

	@Test
	void createPerformanceRejectsInvalidScheduleWindow() {
		PerformanceDto.PerformanceCreateRequest request = new PerformanceDto.PerformanceCreateRequest(
				"2026 Summer Concert",
				LocalDate.of(2026, 8, 20),
				"Main Hall",
				LocalDate.of(2026, 8, 1),
				LocalDate.of(2026, 8, 21)
		);

		assertThatThrownBy(() -> performanceService.createPerformance(request))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Schedule window end date must not be after performance date");
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

	@Test
	void deletePerformanceMemberDeletesChildrenButKeepsUser() {
		Performance performance = performanceRepository.save(Performance.create(
				"2026 Summer Concert",
				LocalDate.of(2026, 8, 15),
				"Main Hall"
		));
		User user = userRepository.save(User.create("Kim Vocal", "20261234"));
		PerformanceMember performanceMember = performanceMemberRepository.save(PerformanceMember.create(performance, user));
		Team team = teamRepository.save(Team.create(performance, "Team A", "Song A"));
		TeamMember teamMember = teamMemberRepository.save(TeamMember.create(team, performanceMember, Part.VOCAL));
		AvailableTime availableTime = availabilityRepository.save(AvailableTime.create(
				teamMember,
				LocalDateTime.of(2026, 8, 1, 15, 0),
				LocalDateTime.of(2026, 8, 1, 18, 0)
		));
		SongRequest songRequest = songRequestRepository.save(SongRequest.create(performance, team, performanceMember, "Song A - Artist A"));
		SongVote songVote = songVoteRepository.save(SongVote.create(songRequest, performanceMember, Vote.POSSIBLE, null));

		performanceService.deletePerformanceMember(performanceMember.getId());

		assertThat(songVoteRepository.findById(songVote.getId())).isEmpty();
		assertThat(songRequestRepository.findById(songRequest.getId())).isEmpty();
		assertThat(availabilityRepository.findById(availableTime.getId())).isEmpty();
		assertThat(teamMemberRepository.findById(teamMember.getId())).isEmpty();
		assertThat(performanceMemberRepository.findById(performanceMember.getId())).isEmpty();
		assertThat(userRepository.findById(user.getId())).isPresent();
		assertThat(teamRepository.findById(team.getId())).isPresent();
	}

	@Test
	void deletePerformanceDeletesAllPerformanceChildrenButKeepsUsers() {
		Performance performance = performanceRepository.save(Performance.create(
				"2026 Summer Concert",
				LocalDate.of(2026, 8, 15),
				"Main Hall"
		));
		User user = userRepository.save(User.create("Kim Vocal", "20261234"));
		PerformanceMember performanceMember = performanceMemberRepository.save(PerformanceMember.create(performance, user));
		Team team = teamRepository.save(Team.create(performance, "Team A", "Song A"));
		TeamMember teamMember = teamMemberRepository.save(TeamMember.create(team, performanceMember, Part.VOCAL));
		InputLink inputLink = inputLinkRepository.save(InputLink.create("available-token", performance, InputLinkType.AVAILABLE_TIME, true, null));
		PerformanceConfirmedSong confirmedSong = performanceConfirmedSongRepository.save(PerformanceConfirmedSong.create(performance, "Song A - Artist A"));
		AvailableTime availableTime = availabilityRepository.save(AvailableTime.create(
				teamMember,
				LocalDateTime.of(2026, 8, 1, 15, 0),
				LocalDateTime.of(2026, 8, 1, 18, 0)
		));
		FinalSchedule finalSchedule = finalScheduleRepository.save(FinalSchedule.create(
				team,
				LocalDateTime.of(2026, 8, 2, 15, 0),
				LocalDateTime.of(2026, 8, 2, 18, 0),
				null
		));
		SongRequest songRequest = songRequestRepository.save(SongRequest.create(performance, team, performanceMember, "Song A - Artist A"));
		SongVote songVote = songVoteRepository.save(SongVote.create(songRequest, performanceMember, Vote.POSSIBLE, null));

		performanceService.deletePerformance(performance.getId());

		assertThat(inputLinkRepository.findById(inputLink.getId())).isEmpty();
		assertThat(performanceConfirmedSongRepository.findById(confirmedSong.getId())).isEmpty();
		assertThat(finalScheduleRepository.findById(finalSchedule.getId())).isEmpty();
		assertThat(songVoteRepository.findById(songVote.getId())).isEmpty();
		assertThat(songRequestRepository.findById(songRequest.getId())).isEmpty();
		assertThat(availabilityRepository.findById(availableTime.getId())).isEmpty();
		assertThat(teamMemberRepository.findById(teamMember.getId())).isEmpty();
		assertThat(teamRepository.findById(team.getId())).isEmpty();
		assertThat(performanceMemberRepository.findById(performanceMember.getId())).isEmpty();
		assertThat(performanceRepository.findById(performance.getId())).isEmpty();
		assertThat(userRepository.findById(user.getId())).isPresent();
	}
}
