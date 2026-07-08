package io.github.juc211.band_schedule.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.juc211.band_schedule.domain.FinalSchedule;
import io.github.juc211.band_schedule.domain.InputLink;
import io.github.juc211.band_schedule.domain.InputLinkType;
import io.github.juc211.band_schedule.domain.Performance;
import io.github.juc211.band_schedule.domain.Team;
import io.github.juc211.band_schedule.repository.FinalScheduleRepository;
import io.github.juc211.band_schedule.repository.InputLinkRepository;
import io.github.juc211.band_schedule.repository.PerformanceRepository;
import io.github.juc211.band_schedule.repository.TeamRepository;
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
class FinalScheduleControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private FinalScheduleRepository finalScheduleRepository;

	@Autowired
	private PerformanceRepository performanceRepository;

	@Autowired
	private TeamRepository teamRepository;

	@Autowired
	private InputLinkRepository inputLinkRepository;

	@Test
	void createFinalScheduleReturnsCreatedStatus() throws Exception {
		Team team = createTeam();

		mockMvc.perform(post("/api/teams/{teamId}/final-schedules", team.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "startDateTime": "2026-08-21T18:00:00",
								  "endDateTime": "2026-08-21T20:00:00",
								  "memo": "관리자 임의 추가"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.finalScheduleId").isNumber())
				.andExpect(jsonPath("$.teamId").value(team.getId()))
				.andExpect(jsonPath("$.performanceId").value(team.getPerformance().getId()))
				.andExpect(jsonPath("$.teamName").value("Team A"))
				.andExpect(jsonPath("$.confirmedSong").value("Song A"))
				.andExpect(jsonPath("$.startDateTime").value("2026-08-21T18:00:00"))
				.andExpect(jsonPath("$.endDateTime").value("2026-08-21T20:00:00"))
				.andExpect(jsonPath("$.memo").value("관리자 임의 추가"));
	}

	@Test
	void getFinalSchedulesByTeamReturnsOkStatus() throws Exception {
		Team team = createTeam();
		FinalSchedule finalSchedule = finalScheduleRepository.save(FinalSchedule.create(
				team,
				LocalDateTime.of(2026, 8, 1, 15, 0),
				LocalDateTime.of(2026, 8, 1, 17, 0),
				null
		));

		mockMvc.perform(get("/api/teams/{teamId}/final-schedules", team.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].finalScheduleId").value(finalSchedule.getId()))
				.andExpect(jsonPath("$[0].teamId").value(team.getId()))
				.andExpect(jsonPath("$[0].startDateTime").value("2026-08-01T15:00:00"))
				.andExpect(jsonPath("$[0].endDateTime").value("2026-08-01T17:00:00"));
	}

	@Test
	void getFinalSchedulesByPerformanceReturnsOkStatus() throws Exception {
		Performance performance = createPerformance();
		Team firstTeam = teamRepository.save(Team.create(performance, "Team A", "Song A"));
		Team secondTeam = teamRepository.save(Team.create(performance, "Team B", "Song B"));
		finalScheduleRepository.save(FinalSchedule.create(
				firstTeam,
				LocalDateTime.of(2026, 8, 1, 15, 0),
				LocalDateTime.of(2026, 8, 1, 17, 0),
				null
		));
		finalScheduleRepository.save(FinalSchedule.create(
				secondTeam,
				LocalDateTime.of(2026, 8, 7, 22, 0),
				LocalDateTime.of(2026, 8, 7, 23, 0),
				null
		));

		mockMvc.perform(get("/api/performances/{performanceId}/final-schedules", performance.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].teamName").value("Team A"))
				.andExpect(jsonPath("$[1].teamName").value("Team B"));
	}

	@Test
	void getFinalSchedulesByLinkReturnsOkStatus() throws Exception {
		Performance performance = createPerformance();
		inputLinkRepository.save(InputLink.create("view-token", performance, InputLinkType.FINAL_SCHEDULE_VIEW, true, null));
		Team firstTeam = teamRepository.save(Team.create(performance, "Team A", "Song A"));
		Team secondTeam = teamRepository.save(Team.create(performance, "Team B", "Song B"));
		finalScheduleRepository.save(FinalSchedule.create(
				firstTeam,
				LocalDateTime.of(2026, 8, 1, 15, 0),
				LocalDateTime.of(2026, 8, 1, 17, 0),
				null
		));
		finalScheduleRepository.save(FinalSchedule.create(
				secondTeam,
				LocalDateTime.of(2026, 8, 7, 22, 0),
				LocalDateTime.of(2026, 8, 7, 23, 0),
				null
		));

		mockMvc.perform(get("/api/input-links/{token}/final-schedules", "view-token"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].teamName").value("Team A"))
				.andExpect(jsonPath("$[1].teamName").value("Team B"));
	}

	@Test
	void getFinalSchedulesByLinkAndTeamReturnsOkStatus() throws Exception {
		Performance performance = createPerformance();
		inputLinkRepository.save(InputLink.create("view-token", performance, InputLinkType.FINAL_SCHEDULE_VIEW, true, null));
		Team firstTeam = teamRepository.save(Team.create(performance, "Team A", "Song A"));
		Team secondTeam = teamRepository.save(Team.create(performance, "Team B", "Song B"));
		finalScheduleRepository.save(FinalSchedule.create(
				firstTeam,
				LocalDateTime.of(2026, 8, 1, 15, 0),
				LocalDateTime.of(2026, 8, 1, 17, 0),
				null
		));
		FinalSchedule secondSchedule = finalScheduleRepository.save(FinalSchedule.create(
				secondTeam,
				LocalDateTime.of(2026, 8, 7, 22, 0),
				LocalDateTime.of(2026, 8, 7, 23, 0),
				null
		));

		mockMvc.perform(get("/api/input-links/{token}/teams/{teamId}/final-schedules", "view-token", secondTeam.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].finalScheduleId").value(secondSchedule.getId()))
				.andExpect(jsonPath("$[0].teamName").value("Team B"));
	}

	@Test
	void updateFinalScheduleReturnsOkStatus() throws Exception {
		Team team = createTeam();
		FinalSchedule finalSchedule = finalScheduleRepository.save(FinalSchedule.create(
				team,
				LocalDateTime.of(2026, 8, 1, 15, 0),
				LocalDateTime.of(2026, 8, 1, 17, 0),
				null
		));

		mockMvc.perform(patch("/api/final-schedules/{finalScheduleId}", finalSchedule.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "startDateTime": "2026-08-07T22:00:00",
								  "endDateTime": "2026-08-07T23:00:00",
								  "memo": "추가 합주"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.finalScheduleId").value(finalSchedule.getId()))
				.andExpect(jsonPath("$.startDateTime").value("2026-08-07T22:00:00"))
				.andExpect(jsonPath("$.endDateTime").value("2026-08-07T23:00:00"))
				.andExpect(jsonPath("$.memo").value("추가 합주"));
	}

	@Test
	void deleteFinalScheduleReturnsNoContentStatus() throws Exception {
		Team team = createTeam();
		FinalSchedule finalSchedule = finalScheduleRepository.save(FinalSchedule.create(
				team,
				LocalDateTime.of(2026, 8, 1, 15, 0),
				LocalDateTime.of(2026, 8, 1, 17, 0),
				null
		));

		mockMvc.perform(delete("/api/final-schedules/{finalScheduleId}", finalSchedule.getId()))
				.andExpect(status().isNoContent());
	}

	private Team createTeam() {
		return teamRepository.save(Team.create(createPerformance(), "Team A", "Song A"));
	}

	private Performance createPerformance() {
		return performanceRepository.save(Performance.create(
				"2026 Summer Concert",
				LocalDate.of(2026, 8, 20),
				"Main Hall",
				LocalDate.of(2026, 8, 1),
				LocalDate.of(2026, 8, 20)
		));
	}
}
