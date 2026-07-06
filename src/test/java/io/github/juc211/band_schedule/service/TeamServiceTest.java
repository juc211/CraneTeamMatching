package io.github.juc211.band_schedule.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.juc211.band_schedule.domain.InputLink;
import io.github.juc211.band_schedule.domain.InputLinkType;
import io.github.juc211.band_schedule.domain.AvailableTime;
import io.github.juc211.band_schedule.domain.FinalSchedule;
import io.github.juc211.band_schedule.domain.Part;
import io.github.juc211.band_schedule.domain.Performance;
import io.github.juc211.band_schedule.domain.PerformanceMember;
import io.github.juc211.band_schedule.domain.PerformanceConfirmedSong;
import io.github.juc211.band_schedule.domain.SongRequest;
import io.github.juc211.band_schedule.domain.SongVote;
import io.github.juc211.band_schedule.domain.Team;
import io.github.juc211.band_schedule.domain.TeamMember;
import io.github.juc211.band_schedule.domain.User;
import io.github.juc211.band_schedule.domain.Vote;
import io.github.juc211.band_schedule.dto.TeamDto;
import io.github.juc211.band_schedule.repository.AvailabilityRepository;
import io.github.juc211.band_schedule.repository.FinalScheduleRepository;
import io.github.juc211.band_schedule.repository.InputLinkRepository;
import io.github.juc211.band_schedule.repository.PerformanceConfirmedSongRepository;
import io.github.juc211.band_schedule.repository.PerformanceMemberRepository;
import io.github.juc211.band_schedule.repository.PerformanceRepository;
import io.github.juc211.band_schedule.repository.SongRequestRepository;
import io.github.juc211.band_schedule.repository.SongVoteRepository;
import io.github.juc211.band_schedule.repository.TeamMemberRepository;
import io.github.juc211.band_schedule.repository.TeamRepository;
import io.github.juc211.band_schedule.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class TeamServiceTest {

	@Autowired
	private TeamService teamService;

	@Autowired
	private PerformanceRepository performanceRepository;

	@Autowired
	private TeamRepository teamRepository;

	@Autowired
	private PerformanceConfirmedSongRepository performanceConfirmedSongRepository;

	@Autowired
	private TeamMemberRepository teamMemberRepository;

	@Autowired
	private PerformanceMemberRepository performanceMemberRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private InputLinkRepository inputLinkRepository;

	@Autowired
	private AvailabilityRepository availabilityRepository;

	@Autowired
	private FinalScheduleRepository finalScheduleRepository;

	@Autowired
	private SongRequestRepository songRequestRepository;

	@Autowired
	private SongVoteRepository songVoteRepository;

	@Test
	void createTeamPersistsTeamInPerformance() {
		Performance performance = performanceRepository.save(
				Performance.create("2026 Summer Concert", LocalDate.of(2026, 8, 15), "Main Hall")
		);

		TeamDto.TeamResponse response = teamService.createTeam(
				performance.getId(),
				new TeamDto.TeamCreateRequest("Team A", "Song A", null)
		);

		Team savedTeam = teamRepository.findById(response.teamId()).orElseThrow();
		assertThat(savedTeam.getPerformance().getId()).isEqualTo(performance.getId());
		assertThat(savedTeam.getName()).isEqualTo("Team A");
		assertThat(savedTeam.getConfirmedSong()).isEqualTo("Song A");
	}

	@Test
	void updateTeamChangesNameAndPerformanceConfirmedSong() {
		Performance performance = performanceRepository.save(
				Performance.create("2026 Summer Concert", LocalDate.of(2026, 8, 15), "Main Hall")
		);
		Team team = teamRepository.save(Team.create(performance, "Team A", "Song A"));

		TeamDto.TeamResponse response = teamService.updateTeam(
				team.getId(),
				new TeamDto.TeamUpdateRequest("Team B", "Song B", null)
		);

		assertThat(response.name()).isEqualTo("Team B");
		assertThat(response.confirmedSong()).isEqualTo("Song B");
		assertThat(teamRepository.findById(team.getId()).orElseThrow().getName()).isEqualTo("Team B");
	}

	@Test
	void updateTeamConfirmedSongSetsConfirmedSongWhenEmpty() {
		Performance performance = performanceRepository.save(
				Performance.create("2026 Summer Concert", LocalDate.of(2026, 8, 15), "Main Hall")
		);
		Team team = teamRepository.save(Team.create(performance, "Team A", null));

		TeamDto.TeamResponse response = teamService.updateTeamConfirmedSong(
				team.getId(),
				new TeamDto.TeamConfirmedSongUpdateRequest("Confirmed Song - Artist A", null)
		);

		Team savedTeam = teamRepository.findById(team.getId()).orElseThrow();
		assertThat(response.teamId()).isEqualTo(team.getId());
		assertThat(response.name()).isEqualTo("Team A");
		assertThat(response.confirmedSong()).isEqualTo("Confirmed Song - Artist A");
		assertThat(savedTeam.getName()).isEqualTo("Team A");
		assertThat(savedTeam.getConfirmedSong()).isEqualTo("Confirmed Song - Artist A");
	}

	@Test
	void updateTeamConfirmedSongChangesExistingConfirmedSong() {
		Performance performance = performanceRepository.save(
				Performance.create("2026 Summer Concert", LocalDate.of(2026, 8, 15), "Main Hall")
		);
		Team team = teamRepository.save(Team.create(performance, "Team A", "Old Song"));

		TeamDto.TeamResponse response = teamService.updateTeamConfirmedSong(
				team.getId(),
				new TeamDto.TeamConfirmedSongUpdateRequest("New Song", null)
		);

		assertThat(response.teamId()).isEqualTo(team.getId());
		assertThat(response.name()).isEqualTo("Team A");
		assertThat(response.confirmedSong()).isEqualTo("New Song");
		assertThat(teamRepository.findById(team.getId()).orElseThrow().getConfirmedSong()).isEqualTo("New Song");
	}

	@Test
	void getTeamConfirmedSongReturnsTeamConfirmedSong() {
		Performance performance = performanceRepository.save(
				Performance.create("2026 Summer Concert", LocalDate.of(2026, 8, 15), "Main Hall")
		);
		Team team = teamRepository.save(Team.create(performance, "Team A", "Confirmed Song - Artist A"));

		TeamDto.TeamConfirmedSongResponse response = teamService.getTeamConfirmedSong(team.getId());

		assertThat(response.teamId()).isEqualTo(team.getId());
		assertThat(response.confirmedSong()).isEqualTo("Confirmed Song - Artist A");
	}

	@Test
	void deleteTeamConfirmedSongClearsConfirmedSong() {
		Performance performance = performanceRepository.save(
				Performance.create("2026 Summer Concert", LocalDate.of(2026, 8, 15), "Main Hall")
		);
		Team team = teamRepository.save(Team.create(performance, "Team A", "Confirmed Song - Artist A"));

		teamService.deleteTeamConfirmedSong(team.getId());

		assertThat(teamRepository.findById(team.getId()).orElseThrow().getConfirmedSong()).isNull();
	}

	@Test
	void createTeamCanUsePerformanceConfirmedSong() {
		Performance performance = performanceRepository.save(
				Performance.create("2026 Summer Concert", LocalDate.of(2026, 8, 15), "Main Hall")
		);
		PerformanceConfirmedSong confirmedSong = performanceConfirmedSongRepository.save(
				PerformanceConfirmedSong.create(performance, "Confirmed Song A - Artist A")
		);

		TeamDto.TeamResponse response = teamService.createTeam(
				performance.getId(),
				new TeamDto.TeamCreateRequest("Team A", null, confirmedSong.getId())
		);

		Team savedTeam = teamRepository.findById(response.teamId()).orElseThrow();
		assertThat(response.confirmedSong()).isEqualTo("Confirmed Song A - Artist A");
		assertThat(savedTeam.getConfirmedSong()).isEqualTo("Confirmed Song A - Artist A");
	}

	@Test
	void updateTeamConfirmedSongCanUsePerformanceConfirmedSong() {
		Performance performance = performanceRepository.save(
				Performance.create("2026 Summer Concert", LocalDate.of(2026, 8, 15), "Main Hall")
		);
		PerformanceConfirmedSong confirmedSong = performanceConfirmedSongRepository.save(
				PerformanceConfirmedSong.create(performance, "Confirmed Song A - Artist A")
		);
		Team team = teamRepository.save(Team.create(performance, "Team A", null));

		TeamDto.TeamResponse response = teamService.updateTeamConfirmedSong(
				team.getId(),
				new TeamDto.TeamConfirmedSongUpdateRequest(null, confirmedSong.getId())
		);

		assertThat(response.confirmedSong()).isEqualTo("Confirmed Song A - Artist A");
		assertThat(teamRepository.findById(team.getId()).orElseThrow().getConfirmedSong())
				.isEqualTo("Confirmed Song A - Artist A");
	}

	@Test
	void createTeamRejectsPerformanceConfirmedSongFromDifferentPerformance() {
		Performance firstPerformance = performanceRepository.save(
				Performance.create("2026 Summer Concert", LocalDate.of(2026, 8, 15), "Main Hall")
		);
		Performance secondPerformance = performanceRepository.save(
				Performance.create("2026 Winter Concert", LocalDate.of(2026, 12, 20), "Club Room")
		);
		PerformanceConfirmedSong otherPerformanceConfirmedSong = performanceConfirmedSongRepository.save(
				PerformanceConfirmedSong.create(secondPerformance, "Other Performance Song")
		);

		assertThatThrownBy(() -> teamService.createTeam(
				firstPerformance.getId(),
				new TeamDto.TeamCreateRequest("Team A", null, otherPerformanceConfirmedSong.getId())
		))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("PerformanceConfirmedSong does not belong to team performance");
	}

	@Test
	void getTeamsByPerformanceReturnsTeamsInPerformance() {
		Performance performance = performanceRepository.save(
				Performance.create("2026 Summer Concert", LocalDate.of(2026, 8, 15), "Main Hall")
		);
		teamRepository.save(Team.create(performance, "Team A", "Song A"));
		teamRepository.save(Team.create(performance, "Team B", "Song B"));

		assertThat(teamService.getTeamsByPerformance(performance.getId()))
				.extracting(TeamDto.TeamResponse::name)
				.containsExactly("Team A", "Team B");
	}

	@Test
	void getTeamsByLinkReturnsTeamsInLinkPerformance() {
		Performance performance = performanceRepository.save(
				Performance.create("2026 Summer Concert", LocalDate.of(2026, 8, 15), "Main Hall")
		);
		Performance otherPerformance = performanceRepository.save(
				Performance.create("2026 Winter Concert", LocalDate.of(2026, 12, 20), "Club Room")
		);
		inputLinkRepository.save(InputLink.create("available-token", performance, InputLinkType.AVAILABLE_TIME, true, null));
		teamRepository.save(Team.create(performance, "Team A", "Song A"));
		teamRepository.save(Team.create(performance, "Team B", "Song B"));
		teamRepository.save(Team.create(otherPerformance, "Other Team", "Other Song"));

		assertThat(teamService.getTeamsByLink("available-token"))
				.extracting(TeamDto.TeamResponse::name)
				.containsExactly("Team A", "Team B");
	}

	@Test
	void getTeamsByLinkAllowsSongRequestLink() {
		Performance performance = performanceRepository.save(
				Performance.create("2026 Summer Concert", LocalDate.of(2026, 8, 15), "Main Hall")
		);
		inputLinkRepository.save(InputLink.create("song-token", performance, InputLinkType.SONG_REQUEST, true, null));
		teamRepository.save(Team.create(performance, "Team A", "Song A"));

		assertThat(teamService.getTeamsByLink("song-token"))
				.extracting(TeamDto.TeamResponse::name)
				.containsExactly("Team A");
	}

	@Test
	void addTeamMemberAllowsSamePerformanceMemberInMultipleTeamsAndParts() {
		Performance performance = performanceRepository.save(
				Performance.create("2026 Summer Concert", LocalDate.of(2026, 8, 15), "Main Hall")
		);
		User user = userRepository.save(User.create("Kim Multi", "20261234"));
		PerformanceMember performanceMember = performanceMemberRepository.save(PerformanceMember.create(performance, user));
		Team firstTeam = teamRepository.save(Team.create(performance, "Team A", "Song A"));
		Team secondTeam = teamRepository.save(Team.create(performance, "Team B", "Song B"));

		TeamDto.TeamMemberResponse firstResponse = teamService.addTeamMember(
				firstTeam.getId(),
				new TeamDto.TeamMemberAddRequest(performanceMember.getId(), Part.VOCAL)
		);
		TeamDto.TeamMemberResponse secondResponse = teamService.addTeamMember(
				secondTeam.getId(),
				new TeamDto.TeamMemberAddRequest(performanceMember.getId(), Part.GUITAR)
		);

		assertThat(teamMemberRepository.findAll()).hasSize(2);
		assertThat(firstResponse.part()).isEqualTo(Part.VOCAL);
		assertThat(secondResponse.part()).isEqualTo(Part.GUITAR);
		TeamMember firstMember = teamMemberRepository.findById(firstResponse.teamMemberId()).orElseThrow();
		TeamMember secondMember = teamMemberRepository.findById(secondResponse.teamMemberId()).orElseThrow();
		assertThat(firstMember.getPerformanceMember().getId()).isEqualTo(performanceMember.getId());
		assertThat(secondMember.getPerformanceMember().getId()).isEqualTo(performanceMember.getId());
	}

	@Test
	void getTeamMembersReturnsMembersInTeam() {
		Performance performance = performanceRepository.save(
				Performance.create("2026 Summer Concert", LocalDate.of(2026, 8, 15), "Main Hall")
		);
		User firstUser = userRepository.save(User.create("Kim Vocal", "20261234"));
		User secondUser = userRepository.save(User.create("Lee Guitar", "20261235"));
		PerformanceMember firstPerformanceMember = performanceMemberRepository.save(PerformanceMember.create(performance, firstUser));
		PerformanceMember secondPerformanceMember = performanceMemberRepository.save(PerformanceMember.create(performance, secondUser));
		Team team = teamRepository.save(Team.create(performance, "Team A", "Song A"));
		teamMemberRepository.save(TeamMember.create(team, firstPerformanceMember, Part.VOCAL));
		teamMemberRepository.save(TeamMember.create(team, secondPerformanceMember, Part.GUITAR));

		assertThat(teamService.getTeamMembers(team.getId()))
				.extracting(TeamDto.TeamMemberResponse::name)
				.containsExactly("Kim Vocal", "Lee Guitar");
	}

	@Test
	void getTeamMembersByLinkReturnsTeamMembersInLinkPerformance() {
		Performance performance = performanceRepository.save(
				Performance.create("2026 Summer Concert", LocalDate.of(2026, 8, 15), "Main Hall")
		);
		inputLinkRepository.save(InputLink.create("available-token", performance, InputLinkType.AVAILABLE_TIME, true, null));
		User user = userRepository.save(User.create("Kim Vocal", "20261234"));
		PerformanceMember performanceMember = performanceMemberRepository.save(PerformanceMember.create(performance, user));
		Team team = teamRepository.save(Team.create(performance, "Team A", "Song A"));
		teamMemberRepository.save(TeamMember.create(team, performanceMember, Part.VOCAL));

		assertThat(teamService.getTeamMembersByLink("available-token", team.getId()))
				.extracting(TeamDto.TeamMemberResponse::name)
				.containsExactly("Kim Vocal");
	}

	@Test
	void getTeamMembersByLinkRejectsTeamFromDifferentPerformance() {
		Performance performance = performanceRepository.save(
				Performance.create("2026 Summer Concert", LocalDate.of(2026, 8, 15), "Main Hall")
		);
		Performance otherPerformance = performanceRepository.save(
				Performance.create("2026 Winter Concert", LocalDate.of(2026, 12, 20), "Club Room")
		);
		inputLinkRepository.save(InputLink.create("available-token", performance, InputLinkType.AVAILABLE_TIME, true, null));
		Team otherTeam = teamRepository.save(Team.create(otherPerformance, "Other Team", "Other Song"));

		assertThatThrownBy(() -> teamService.getTeamMembersByLink("available-token", otherTeam.getId()))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Team does not belong to link performance");
	}

	@Test
	void deleteTeamMemberDeletesAvailableTimesOnly() {
		Performance performance = performanceRepository.save(
				Performance.create("2026 Summer Concert", LocalDate.of(2026, 8, 15), "Main Hall")
		);
		User user = userRepository.save(User.create("Kim Vocal", "20261234"));
		PerformanceMember performanceMember = performanceMemberRepository.save(PerformanceMember.create(performance, user));
		Team team = teamRepository.save(Team.create(performance, "Team A", "Song A"));
		TeamMember teamMember = teamMemberRepository.save(TeamMember.create(team, performanceMember, Part.VOCAL));
		AvailableTime availableTime = availabilityRepository.save(AvailableTime.create(
				teamMember,
				LocalDateTime.of(2026, 8, 1, 15, 0),
				LocalDateTime.of(2026, 8, 1, 18, 0)
		));

		teamService.deleteTeamMember(teamMember.getId());

		assertThat(availabilityRepository.findById(availableTime.getId())).isEmpty();
		assertThat(teamMemberRepository.findById(teamMember.getId())).isEmpty();
		assertThat(performanceMemberRepository.findById(performanceMember.getId())).isPresent();
		assertThat(userRepository.findById(user.getId())).isPresent();
	}

	@Test
	void deleteTeamDeletesTeamChildren() {
		Performance performance = performanceRepository.save(
				Performance.create("2026 Summer Concert", LocalDate.of(2026, 8, 15), "Main Hall")
		);
		User user = userRepository.save(User.create("Kim Vocal", "20261234"));
		PerformanceMember performanceMember = performanceMemberRepository.save(PerformanceMember.create(performance, user));
		Team team = teamRepository.save(Team.create(performance, "Team A", "Song A"));
		TeamMember teamMember = teamMemberRepository.save(TeamMember.create(team, performanceMember, Part.VOCAL));
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

		teamService.deleteTeam(team.getId());

		assertThat(songVoteRepository.findById(songVote.getId())).isEmpty();
		assertThat(songRequestRepository.findById(songRequest.getId())).isEmpty();
		assertThat(finalScheduleRepository.findById(finalSchedule.getId())).isEmpty();
		assertThat(availabilityRepository.findById(availableTime.getId())).isEmpty();
		assertThat(teamMemberRepository.findById(teamMember.getId())).isEmpty();
		assertThat(teamRepository.findById(team.getId())).isEmpty();
		assertThat(performanceMemberRepository.findById(performanceMember.getId())).isPresent();
	}

	@Test
	void addTeamMemberRejectsPerformanceMemberFromDifferentPerformance() {
		Performance firstPerformance = performanceRepository.save(
				Performance.create("2026 Summer Concert", LocalDate.of(2026, 8, 15), "Main Hall")
		);
		Performance secondPerformance = performanceRepository.save(
				Performance.create("2026 Winter Concert", LocalDate.of(2026, 12, 20), "Club Room")
		);
		User user = userRepository.save(User.create("Kim Multi", "20261234"));
		PerformanceMember otherPerformanceMember = performanceMemberRepository.save(PerformanceMember.create(secondPerformance, user));
		Team team = teamRepository.save(Team.create(firstPerformance, "Team A", "Song A"));

		assertThatThrownBy(() -> teamService.addTeamMember(
				team.getId(),
				new TeamDto.TeamMemberAddRequest(otherPerformanceMember.getId(), Part.VOCAL)
		))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("PerformanceMember does not belong to team performance");
	}
}
