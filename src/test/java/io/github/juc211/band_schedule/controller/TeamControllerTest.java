package io.github.juc211.band_schedule.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.juc211.band_schedule.domain.Part;
import io.github.juc211.band_schedule.domain.Performance;
import io.github.juc211.band_schedule.domain.PerformanceMember;
import io.github.juc211.band_schedule.domain.Team;
import io.github.juc211.band_schedule.domain.TeamMember;
import io.github.juc211.band_schedule.domain.User;
import io.github.juc211.band_schedule.repository.PerformanceMemberRepository;
import io.github.juc211.band_schedule.repository.PerformanceRepository;
import io.github.juc211.band_schedule.repository.TeamMemberRepository;
import io.github.juc211.band_schedule.repository.TeamRepository;
import io.github.juc211.band_schedule.repository.UserRepository;
import java.time.LocalDate;
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
class TeamControllerTest {

	@Autowired
	private MockMvc mockMvc;

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
	void createAndUpdateTeam() throws Exception {
		Performance performance = performanceRepository.save(
				Performance.create("2026 Summer Concert", LocalDate.of(2026, 8, 15), "Main Hall")
		);

		String createdTeamResponse = mockMvc.perform(post("/api/performances/{performanceId}/teams", performance.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "Team A",
								  "confirmedSong": "Song A"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value("Team A"))
				.andExpect(jsonPath("$.confirmedSong").value("Song A"))
				.andReturn()
				.getResponse()
				.getContentAsString();

		Long teamId = Long.valueOf(createdTeamResponse.replaceAll(".*\\\"teamId\\\":(\\d+).*", "$1"));

		mockMvc.perform(patch("/api/teams/{teamId}", teamId)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "Team B",
								  "confirmedSong": "Song B"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.teamId").value(teamId))
				.andExpect(jsonPath("$.name").value("Team B"))
				.andExpect(jsonPath("$.confirmedSong").value("Song B"));
	}

	@Test
	void getTeamsByPerformanceReturnsOkStatus() throws Exception {
		Performance performance = performanceRepository.save(
				Performance.create("2026 Summer Concert", LocalDate.of(2026, 8, 15), "Main Hall")
		);
		Team team = teamRepository.save(Team.create(performance, "Team A", "Song A"));

		mockMvc.perform(get("/api/performances/{performanceId}/teams", performance.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].teamId").value(team.getId()))
				.andExpect(jsonPath("$[0].performanceId").value(performance.getId()))
				.andExpect(jsonPath("$[0].name").value("Team A"))
				.andExpect(jsonPath("$[0].confirmedSong").value("Song A"));
	}

	@Test
	void addTeamMember() throws Exception {
		Performance performance = performanceRepository.save(
				Performance.create("2026 Summer Concert", LocalDate.of(2026, 8, 15), "Main Hall")
		);
		User user = userRepository.save(User.create("Kim Multi", "20261234"));
		PerformanceMember performanceMember = performanceMemberRepository.save(PerformanceMember.create(performance, user));
		Team firstTeam = teamRepository.save(Team.create(performance, "Team A", "Song A"));

		mockMvc.perform(post("/api/teams/{teamId}/members", firstTeam.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "performanceMemberId": %d,
								  "part": "VOCAL"
								}
								""".formatted(performanceMember.getId())))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.teamId").value(firstTeam.getId()))
				.andExpect(jsonPath("$.performanceMemberId").value(performanceMember.getId()))
				.andExpect(jsonPath("$.part").value("VOCAL"));
	}

	@Test
	void getTeamMembersReturnsOkStatus() throws Exception {
		Performance performance = performanceRepository.save(
				Performance.create("2026 Summer Concert", LocalDate.of(2026, 8, 15), "Main Hall")
		);
		User user = userRepository.save(User.create("Kim Multi", "20261234"));
		PerformanceMember performanceMember = performanceMemberRepository.save(PerformanceMember.create(performance, user));
		Team team = teamRepository.save(Team.create(performance, "Team A", "Song A"));
		TeamMember teamMember = teamMemberRepository.save(TeamMember.create(team, performanceMember, Part.VOCAL));

		mockMvc.perform(get("/api/teams/{teamId}/members", team.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].teamMemberId").value(teamMember.getId()))
				.andExpect(jsonPath("$[0].teamId").value(team.getId()))
				.andExpect(jsonPath("$[0].performanceMemberId").value(performanceMember.getId()))
				.andExpect(jsonPath("$[0].userId").value(user.getId()))
				.andExpect(jsonPath("$[0].name").value("Kim Multi"))
				.andExpect(jsonPath("$[0].part").value("VOCAL"));
	}
}
