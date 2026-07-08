package io.github.juc211.band_schedule.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.juc211.band_schedule.domain.AvailableTime;
import io.github.juc211.band_schedule.domain.InputLink;
import io.github.juc211.band_schedule.domain.InputLinkType;
import io.github.juc211.band_schedule.domain.Part;
import io.github.juc211.band_schedule.domain.Performance;
import io.github.juc211.band_schedule.domain.PerformanceMember;
import io.github.juc211.band_schedule.domain.Team;
import io.github.juc211.band_schedule.domain.TeamMember;
import io.github.juc211.band_schedule.domain.User;
import io.github.juc211.band_schedule.repository.AvailabilityRepository;
import io.github.juc211.band_schedule.repository.InputLinkRepository;
import io.github.juc211.band_schedule.repository.PerformanceMemberRepository;
import io.github.juc211.band_schedule.repository.PerformanceRepository;
import io.github.juc211.band_schedule.repository.TeamMemberRepository;
import io.github.juc211.band_schedule.repository.TeamRepository;
import io.github.juc211.band_schedule.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AvailableTimeControllerTest {

	@Autowired
	private MockMvc mockMvc;

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
	void replaceAvailableTimesByTeamMemberReturnsOkStatus() throws Exception {
		TeamMember teamMember = createTeamMemberWithScheduleWindow();

		mockMvc.perform(put("/api/team-members/{teamMemberId}/available-times", teamMember.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "availableTimes": [
								    {
								      "startDateTime": "2026-08-01T15:00:00",
								      "endDateTime": "2026-08-01T18:00:00"
								    },
								    {
								      "startDateTime": "2026-08-02T16:00:00",
								      "endDateTime": "2026-08-02T20:00:00"
								    }
								  ]
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].availableTimeId").isNumber())
				.andExpect(jsonPath("$[0].teamMemberId").value(teamMember.getId()))
				.andExpect(jsonPath("$[0].teamId").value(teamMember.getTeam().getId()))
				.andExpect(jsonPath("$[0].name").value("Kim Vocal"))
				.andExpect(jsonPath("$[0].startDateTime").value("2026-08-01T15:00:00"))
				.andExpect(jsonPath("$[0].endDateTime").value("2026-08-01T18:00:00"))
				.andExpect(jsonPath("$[1].startDateTime").value("2026-08-02T16:00:00"))
				.andExpect(jsonPath("$[1].endDateTime").value("2026-08-02T20:00:00"));
	}

	@Test
	void getAvailableTimesByTeamMemberReturnsOkStatus() throws Exception {
		TeamMember teamMember = createTeamMemberWithScheduleWindow();
		AvailableTime availableTime = availabilityRepository.save(AvailableTime.create(
				teamMember,
				LocalDateTime.of(2026, 8, 1, 15, 0),
				LocalDateTime.of(2026, 8, 1, 18, 0)
		));

		mockMvc.perform(get("/api/team-members/{teamMemberId}/available-times", teamMember.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].availableTimeId").value(availableTime.getId()))
				.andExpect(jsonPath("$[0].teamMemberId").value(teamMember.getId()))
				.andExpect(jsonPath("$[0].startDateTime").value("2026-08-01T15:00:00"))
				.andExpect(jsonPath("$[0].endDateTime").value("2026-08-01T18:00:00"));
	}

	@Test
	void getAvailableTimesByTeamReturnsOkStatus() throws Exception {
		TeamMember teamMember = createTeamMemberWithScheduleWindow();
		availabilityRepository.save(AvailableTime.create(
				teamMember,
				LocalDateTime.of(2026, 8, 1, 15, 0),
				LocalDateTime.of(2026, 8, 1, 18, 0)
		));

		mockMvc.perform(get("/api/teams/{teamId}/available-times", teamMember.getTeam().getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].teamId").value(teamMember.getTeam().getId()))
				.andExpect(jsonPath("$[0].teamMemberId").value(teamMember.getId()));
	}

	@Test
	void getAvailableTimeOverlapsByTeamReturnsOkStatus() throws Exception {
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

		mockMvc.perform(get("/api/teams/{teamId}/available-time-overlaps", team.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].teamId").value(team.getId()))
				.andExpect(jsonPath("$[0].requiredTeamMemberCount").value(3))
				.andExpect(jsonPath("$[0].availableTeamMemberCount").value(3))
				.andExpect(jsonPath("$[0].startDateTime").value("2026-08-01T16:00:00"))
				.andExpect(jsonPath("$[0].endDateTime").value("2026-08-01T17:00:00"));
	}

	@Test
	void replaceAvailableTimesByTeamMemberCanClearAvailableTimes() throws Exception {
		TeamMember teamMember = createTeamMemberWithScheduleWindow();
		availabilityRepository.save(AvailableTime.create(
				teamMember,
				LocalDateTime.of(2026, 8, 1, 15, 0),
				LocalDateTime.of(2026, 8, 1, 18, 0)
		));

		mockMvc.perform(put("/api/team-members/{teamMemberId}/available-times", teamMember.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "availableTimes": []
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isEmpty());
	}

	@Test
	void replaceAvailableTimesByTeamMemberWithLinkReturnsOkStatus() throws Exception {
		TeamMember teamMember = createTeamMemberWithScheduleWindow();
		inputLinkRepository.save(InputLink.create(
				"available-token",
				teamMember.getTeam().getPerformance(),
				InputLinkType.AVAILABLE_TIME,
				true,
				null
		));

		mockMvc.perform(put("/api/input-links/{token}/team-members/{teamMemberId}/available-times", "available-token", teamMember.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "availableTimes": [
								    {
								      "startDateTime": "2026-08-01T15:00:00",
								      "endDateTime": "2026-08-01T18:00:00"
								    }
								  ]
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].teamMemberId").value(teamMember.getId()))
				.andExpect(jsonPath("$[0].startDateTime").value("2026-08-01T15:00:00"))
				.andExpect(jsonPath("$[0].endDateTime").value("2026-08-01T18:00:00"));
	}

	private TeamMember createTeamMemberWithScheduleWindow() {
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
		return teamMemberRepository.save(TeamMember.create(team, performanceMember, Part.VOCAL));
	}

	private TeamMember createTeamMember(Performance performance, Team team, String name, String studentNumber, Part part) {
		User user = userRepository.save(User.create(name, studentNumber));
		PerformanceMember performanceMember = performanceMemberRepository.save(PerformanceMember.create(performance, user));
		return teamMemberRepository.save(TeamMember.create(team, performanceMember, part));
	}
}
