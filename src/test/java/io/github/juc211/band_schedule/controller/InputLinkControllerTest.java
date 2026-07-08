package io.github.juc211.band_schedule.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.juc211.band_schedule.domain.InputLink;
import io.github.juc211.band_schedule.domain.InputLinkType;
import io.github.juc211.band_schedule.domain.Performance;
import io.github.juc211.band_schedule.repository.InputLinkRepository;
import io.github.juc211.band_schedule.repository.PerformanceRepository;
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

	private Performance createPerformance() {
		return performanceRepository.save(Performance.create(
				"2026 Summer Concert",
				LocalDate.of(2026, 8, 20),
				"Main Hall"
		));
	}
}
