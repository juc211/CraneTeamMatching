package io.github.juc211.band_schedule.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.juc211.band_schedule.domain.Part;
import io.github.juc211.band_schedule.domain.Performance;
import io.github.juc211.band_schedule.domain.PerformanceMember;
import io.github.juc211.band_schedule.domain.Team;
import io.github.juc211.band_schedule.domain.TeamMember;
import io.github.juc211.band_schedule.domain.User;
import io.github.juc211.band_schedule.dto.TeamDto;
import io.github.juc211.band_schedule.repository.PerformanceMemberRepository;
import io.github.juc211.band_schedule.repository.PerformanceRepository;
import io.github.juc211.band_schedule.repository.TeamMemberRepository;
import io.github.juc211.band_schedule.repository.TeamRepository;
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
class TeamServiceTest {

	@Autowired
	private TeamService teamService;

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

	@Test
	void createTeamPersistsTeamInPerformance() {
		Performance performance = performanceRepository.save(
				Performance.create("2026 Summer Concert", LocalDate.of(2026, 8, 15), "Main Hall")
		);

		TeamDto.TeamResponse response = teamService.createTeam(
				performance.getId(),
				new TeamDto.TeamCreateRequest("Team A", "Song A")
		);

		Team savedTeam = teamRepository.findById(response.teamId()).orElseThrow();
		assertThat(savedTeam.getPerformance().getId()).isEqualTo(performance.getId());
		assertThat(savedTeam.getName()).isEqualTo("Team A");
		assertThat(savedTeam.getConfirmedSong()).isEqualTo("Song A");
	}

	@Test
	void updateTeamChangesNameAndConfirmedSong() {
		Performance performance = performanceRepository.save(
				Performance.create("2026 Summer Concert", LocalDate.of(2026, 8, 15), "Main Hall")
		);
		Team team = teamRepository.save(Team.create(performance, "Team A", "Song A"));

		TeamDto.TeamResponse response = teamService.updateTeam(
				team.getId(),
				new TeamDto.TeamUpdateRequest("Team B", "Song B")
		);

		assertThat(response.name()).isEqualTo("Team B");
		assertThat(response.confirmedSong()).isEqualTo("Song B");
		assertThat(teamRepository.findById(team.getId()).orElseThrow().getName()).isEqualTo("Team B");
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
