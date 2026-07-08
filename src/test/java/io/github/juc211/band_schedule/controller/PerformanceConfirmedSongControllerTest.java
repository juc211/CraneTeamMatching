package io.github.juc211.band_schedule.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.juc211.band_schedule.domain.PerformanceConfirmedSong;
import io.github.juc211.band_schedule.domain.Performance;
import io.github.juc211.band_schedule.repository.PerformanceConfirmedSongRepository;
import io.github.juc211.band_schedule.repository.PerformanceRepository;
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
class PerformanceConfirmedSongControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private PerformanceConfirmedSongRepository performanceConfirmedSongRepository;

	@Autowired
	private PerformanceRepository performanceRepository;

	@Test
	void createPerformanceConfirmedSongReturnsCreatedStatus() throws Exception {
		Performance performance = performanceRepository.save(
				Performance.create("2026 Summer Concert", LocalDate.of(2026, 8, 15), "Main Hall")
		);

		mockMvc.perform(post("/api/performances/{performanceId}/performance-confirmed-songs", performance.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "song": "Confirmed Song - Artist A"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.performanceConfirmedSongId").isNumber())
				.andExpect(jsonPath("$.performanceId").value(performance.getId()))
				.andExpect(jsonPath("$.song").value("Confirmed Song - Artist A"));
	}

	@Test
	void getPerformanceConfirmedSongsByPerformanceReturnsOkStatus() throws Exception {
		Performance performance = performanceRepository.save(
				Performance.create("2026 Summer Concert", LocalDate.of(2026, 8, 15), "Main Hall")
		);
		performanceConfirmedSongRepository.save(PerformanceConfirmedSong.create(performance, "Confirmed Song A - Artist A"));
		performanceConfirmedSongRepository.save(PerformanceConfirmedSong.create(performance, "Confirmed Song B - Artist B"));

		mockMvc.perform(get("/api/performances/{performanceId}/performance-confirmed-songs", performance.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].song").value("Confirmed Song A - Artist A"))
				.andExpect(jsonPath("$[1].song").value("Confirmed Song B - Artist B"));
	}

	@Test
	void deletePerformanceConfirmedSongReturnsNoContentStatus() throws Exception {
		Performance performance = performanceRepository.save(
				Performance.create("2026 Summer Concert", LocalDate.of(2026, 8, 15), "Main Hall")
		);
		PerformanceConfirmedSong confirmedSong = performanceConfirmedSongRepository.save(
				PerformanceConfirmedSong.create(performance, "Confirmed Song - Artist A")
		);

		mockMvc.perform(delete("/api/performance-confirmed-songs/{performanceConfirmedSongId}", confirmedSong.getId()))
				.andExpect(status().isNoContent());
	}
}
