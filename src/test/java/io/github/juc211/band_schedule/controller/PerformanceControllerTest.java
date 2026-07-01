package io.github.juc211.band_schedule.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.juc211.band_schedule.domain.Performance;
import io.github.juc211.band_schedule.domain.PerformanceMember;
import io.github.juc211.band_schedule.domain.User;
import io.github.juc211.band_schedule.repository.PerformanceMemberRepository;
import io.github.juc211.band_schedule.repository.PerformanceRepository;
import io.github.juc211.band_schedule.repository.UserRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PerformanceControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private PerformanceRepository performanceRepository;

	@Autowired
	private PerformanceMemberRepository performanceMemberRepository;

	@Autowired
	private UserRepository userRepository;

	@Test
	void createPerformanceReturnsCreatedStatus() throws Exception {
		mockMvc.perform(post("/api/performances")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "2026 Summer Concert",
								  "performanceDate": "2026-08-15",
								  "location": "Main Hall"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.performanceId").isNumber())
				.andExpect(jsonPath("$.title").value("2026 Summer Concert"));
	}

	@Test
	void getPerformancesReturnsOkStatus() throws Exception {
		performanceRepository.save(Performance.create(
				"2026 Summer Concert",
				LocalDate.of(2026, 8, 15),
				"Main Hall"
		));

		mockMvc.perform(get("/api/performances"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].title").value("2026 Summer Concert"))
				.andExpect(jsonPath("$[0].performanceDate").value("2026-08-15"))
				.andExpect(jsonPath("$[0].location").value("Main Hall"));
	}

	@Test
	void getPerformanceReturnsOkStatus() throws Exception {
		Performance performance = performanceRepository.save(Performance.create(
				"2026 Summer Concert",
				LocalDate.of(2026, 8, 15),
				"Main Hall"
		));

		mockMvc.perform(get("/api/performances/{performanceId}", performance.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.performanceId").value(performance.getId()))
				.andExpect(jsonPath("$.title").value("2026 Summer Concert"))
				.andExpect(jsonPath("$.performanceDate").value("2026-08-15"))
				.andExpect(jsonPath("$.location").value("Main Hall"));
	}

	@Test
	void updatePerformanceReturnsOkStatus() throws Exception {
		Performance performance = performanceRepository.save(Performance.create(
				"2026 Summer Concert",
				LocalDate.of(2026, 8, 15),
				"Main Hall"
		));

		mockMvc.perform(patch("/api/performances/{performanceId}", performance.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "2026 Winter Concert",
								  "performanceDate": "2026-12-20",
								  "location": "Club Room"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.performanceId").value(performance.getId()))
				.andExpect(jsonPath("$.title").value("2026 Winter Concert"))
				.andExpect(jsonPath("$.performanceDate").value("2026-12-20"))
				.andExpect(jsonPath("$.location").value("Club Room"));
	}

	@Test
	void getPerformanceMembersReturnsOkStatus() throws Exception {
		Performance performance = performanceRepository.save(Performance.create(
				"2026 Summer Concert",
				LocalDate.of(2026, 8, 15),
				"Main Hall"
		));
		User user = userRepository.save(User.create("Kim Band", "20261234"));
		PerformanceMember performanceMember = performanceMemberRepository.save(PerformanceMember.create(performance, user));

		mockMvc.perform(get("/api/performances/{performanceId}/members", performance.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].performanceMemberId").value(performanceMember.getId()))
				.andExpect(jsonPath("$[0].userId").value(user.getId()))
				.andExpect(jsonPath("$[0].name").value("Kim Band"));
	}
}
