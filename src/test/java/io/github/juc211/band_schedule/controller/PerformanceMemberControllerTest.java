package io.github.juc211.band_schedule.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
class PerformanceMemberControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private PerformanceRepository performanceRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PerformanceMemberRepository performanceMemberRepository;

	@Test
	void addPerformanceMembersReturnsCreatedStatus() throws Exception {
		Performance performance = performanceRepository.save(
				Performance.create("2026 Summer Concert", LocalDate.of(2026, 8, 15), "Main Hall")
		);
		User vocal = userRepository.save(User.create("Kim Vocal", "20261234"));
		User bass = userRepository.save(User.create("Lee Bass", "20261235"));

		mockMvc.perform(post("/api/performances/{performanceId}/members", performance.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "userIds": [%d, %d]
								}
								""".formatted(vocal.getId(), bass.getId())))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.performanceId").value(performance.getId()))
				.andExpect(jsonPath("$.members[0].userId").value(vocal.getId()))
				.andExpect(jsonPath("$.members[0].name").value("Kim Vocal"))
				.andExpect(jsonPath("$.members[1].userId").value(bass.getId()))
				.andExpect(jsonPath("$.members[1].name").value("Lee Bass"));
	}

	@Test
	void deletePerformanceMemberReturnsNoContentStatus() throws Exception {
		Performance performance = performanceRepository.save(
				Performance.create("2026 Summer Concert", LocalDate.of(2026, 8, 15), "Main Hall")
		);
		User user = userRepository.save(User.create("Kim Vocal", "20261234"));
		PerformanceMember performanceMember = performanceMemberRepository.save(PerformanceMember.create(performance, user));

		mockMvc.perform(delete("/api/performance-members/{performanceMemberId}", performanceMember.getId()))
				.andExpect(status().isNoContent());
	}
}
