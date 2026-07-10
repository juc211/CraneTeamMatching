package io.github.juc211.band_schedule.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.juc211.band_schedule.domain.InputLink;
import io.github.juc211.band_schedule.domain.InputLinkType;
import io.github.juc211.band_schedule.domain.Part;
import io.github.juc211.band_schedule.domain.Performance;
import io.github.juc211.band_schedule.domain.PerformanceMember;
import io.github.juc211.band_schedule.domain.Team;
import io.github.juc211.band_schedule.domain.TeamMember;
import io.github.juc211.band_schedule.domain.User;
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
class InputLinkControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private InputLinkRepository inputLinkRepository;

	@Autowired
	private PerformanceRepository performanceRepository;

	@Autowired
	private PerformanceMemberRepository performanceMemberRepository;

	@Autowired
	private TeamRepository teamRepository;

	@Autowired
	private TeamMemberRepository teamMemberRepository;

	@Autowired
	private UserRepository userRepository;

	@Test
	void createInputLinkReturnsCreatedStatus() throws Exception {
		Performance performance = createPerformance();

		mockMvc.perform(post("/api/performances/{performanceId}/input-links", performance.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "type": "AVAILABLE_TIME",
								  "expiresAt": "2026-08-01T23:59:00"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.inputLinkId").isNumber())
				.andExpect(jsonPath("$.performanceId").value(performance.getId()))
				.andExpect(jsonPath("$.type").value("AVAILABLE_TIME"))
				.andExpect(jsonPath("$.token").isString())
				.andExpect(jsonPath("$.active").value(true));
	}

	@Test
	void getInputLinksByPerformanceReturnsOkStatus() throws Exception {
		Performance performance = createPerformance();
		InputLink inputLink = inputLinkRepository.save(
				InputLink.create("available-token", performance, InputLinkType.AVAILABLE_TIME, true, null)
		);

		mockMvc.perform(get("/api/performances/{performanceId}/input-links", performance.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].inputLinkId").value(inputLink.getId()))
				.andExpect(jsonPath("$[0].type").value("AVAILABLE_TIME"))
				.andExpect(jsonPath("$[0].token").value("available-token"));
	}

	@Test
	void updateInputLinkActiveReturnsOkStatus() throws Exception {
		Performance performance = createPerformance();
		InputLink inputLink = inputLinkRepository.save(
				InputLink.create("available-token", performance, InputLinkType.AVAILABLE_TIME, true, null)
		);

		mockMvc.perform(patch("/api/input-links/{inputLinkId}/active", inputLink.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "active": false
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.inputLinkId").value(inputLink.getId()))
				.andExpect(jsonPath("$.active").value(false));
	}

	@Test
	void updateInputLinkExpiresAtReturnsOkStatus() throws Exception {
		Performance performance = createPerformance();
		InputLink inputLink = inputLinkRepository.save(
				InputLink.create(
						"available-token",
						performance,
						InputLinkType.AVAILABLE_TIME,
						true,
						LocalDateTime.of(2026, 8, 1, 23, 59)
				)
		);

		mockMvc.perform(patch("/api/input-links/{inputLinkId}/expires-at", inputLink.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "expiresAt": "2026-08-05T23:59:00"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.inputLinkId").value(inputLink.getId()))
				.andExpect(jsonPath("$.expiresAt").value("2026-08-05T23:59:00"));
	}

	@Test
	void updateInputLinkExpiresAtCanClearExpiresAt() throws Exception {
		Performance performance = createPerformance();
		InputLink inputLink = inputLinkRepository.save(
				InputLink.create(
						"available-token",
						performance,
						InputLinkType.AVAILABLE_TIME,
						true,
						LocalDateTime.of(2026, 8, 1, 23, 59)
				)
		);

		mockMvc.perform(patch("/api/input-links/{inputLinkId}/expires-at", inputLink.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "expiresAt": null
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.inputLinkId").value(inputLink.getId()))
				.andExpect(jsonPath("$.expiresAt").doesNotExist());
	}

	@Test
	void deleteInputLinkReturnsNoContentStatus() throws Exception {
		Performance performance = createPerformance();
		InputLink inputLink = inputLinkRepository.save(
				InputLink.create("available-token", performance, InputLinkType.AVAILABLE_TIME, true, null)
		);

		mockMvc.perform(delete("/api/input-links/{inputLinkId}", inputLink.getId()))
				.andExpect(status().isNoContent());
	}

	@Test
	void identifyPerformanceMemberReturnsMemberAndTeamMemberIds() throws Exception {
		Performance performance = createPerformance();
		InputLink inputLink = inputLinkRepository.save(
				InputLink.create("available-token", performance, InputLinkType.AVAILABLE_TIME, true, null)
		);
		User user = userRepository.save(User.create("김보컬", "20260001"));
		PerformanceMember performanceMember = performanceMemberRepository.save(PerformanceMember.create(performance, user));
		Team team = teamRepository.save(Team.create(performance, "Team A", "Song A"));
		TeamMember teamMember = teamMemberRepository.save(TeamMember.create(team, performanceMember, Part.VOCAL));

		mockMvc.perform(post("/api/input-links/{token}/identify", inputLink.getToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "김보컬",
								  "studentNumber": "20260001"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.performanceId").value(performance.getId()))
				.andExpect(jsonPath("$.performanceMemberId").value(performanceMember.getId()))
				.andExpect(jsonPath("$.userId").value(user.getId()))
				.andExpect(jsonPath("$.name").value("김보컬"))
				.andExpect(jsonPath("$.studentNumber").value("20260001"))
				.andExpect(jsonPath("$.teamMembers[0].teamMemberId").value(teamMember.getId()))
				.andExpect(jsonPath("$.teamMembers[0].teamId").value(team.getId()))
				.andExpect(jsonPath("$.teamMembers[0].teamName").value("Team A"))
				.andExpect(jsonPath("$.teamMembers[0].part").value("VOCAL"));
	}

	private Performance createPerformance() {
		return performanceRepository.save(Performance.create(
				"2026 Summer Concert",
				LocalDate.of(2026, 8, 20),
				"Main Hall"
		));
	}
}
