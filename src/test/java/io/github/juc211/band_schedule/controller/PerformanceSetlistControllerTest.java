package io.github.juc211.band_schedule.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.juc211.band_schedule.domain.Performance;
import io.github.juc211.band_schedule.domain.PerformanceSetlistItem;
import io.github.juc211.band_schedule.domain.Team;
import io.github.juc211.band_schedule.repository.PerformanceRepository;
import io.github.juc211.band_schedule.repository.PerformanceSetlistItemRepository;
import io.github.juc211.band_schedule.repository.TeamRepository;
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
class PerformanceSetlistControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private PerformanceSetlistItemRepository performanceSetlistItemRepository;

	@Autowired
	private PerformanceRepository performanceRepository;

	@Autowired
	private TeamRepository teamRepository;

	@Test
	void replaceSetlistReturnsOkStatus() throws Exception {
		Performance performance = createPerformance();
		Team firstTeam = teamRepository.save(Team.create(performance, "Team A", "Song A"));
		Team secondTeam = teamRepository.save(Team.create(performance, "Team B", "Song B"));

		mockMvc.perform(put("/api/performances/{performanceId}/setlist", performance.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "items": [
								    { "teamId": %d, "sequenceNumber": 1 },
								    { "teamId": %d, "sequenceNumber": 2 }
								  ]
								}
								""".formatted(secondTeam.getId(), firstTeam.getId())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].performanceId").value(performance.getId()))
				.andExpect(jsonPath("$[0].teamId").value(secondTeam.getId()))
				.andExpect(jsonPath("$[0].teamName").value("Team B"))
				.andExpect(jsonPath("$[0].confirmedSong").value("Song B"))
				.andExpect(jsonPath("$[0].sequenceNumber").value(1))
				.andExpect(jsonPath("$[1].teamId").value(firstTeam.getId()))
				.andExpect(jsonPath("$[1].sequenceNumber").value(2));
	}

	@Test
	void getSetlistReturnsOkStatus() throws Exception {
		Performance performance = createPerformance();
		Team firstTeam = teamRepository.save(Team.create(performance, "Team A", "Song A"));
		Team secondTeam = teamRepository.save(Team.create(performance, "Team B", "Song B"));
		performanceSetlistItemRepository.save(PerformanceSetlistItem.create(performance, secondTeam, 1));
		performanceSetlistItemRepository.save(PerformanceSetlistItem.create(performance, firstTeam, 2));

		mockMvc.perform(get("/api/performances/{performanceId}/setlist", performance.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].teamName").value("Team B"))
				.andExpect(jsonPath("$[0].sequenceNumber").value(1))
				.andExpect(jsonPath("$[1].teamName").value("Team A"))
				.andExpect(jsonPath("$[1].sequenceNumber").value(2));
	}

	@Test
	void deleteSetlistReturnsNoContentStatus() throws Exception {
		Performance performance = createPerformance();
		Team team = teamRepository.save(Team.create(performance, "Team A", "Song A"));
		performanceSetlistItemRepository.save(PerformanceSetlistItem.create(performance, team, 1));

		mockMvc.perform(delete("/api/performances/{performanceId}/setlist", performance.getId()))
				.andExpect(status().isNoContent());
	}

	private Performance createPerformance() {
		return performanceRepository.save(Performance.create(
				"2026 Summer Concert",
				LocalDate.of(2026, 8, 20),
				"Main Hall"
		));
	}
}
