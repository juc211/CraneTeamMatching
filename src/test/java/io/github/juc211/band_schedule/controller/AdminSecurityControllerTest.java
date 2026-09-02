package io.github.juc211.band_schedule.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.juc211.band_schedule.domain.InputLink;
import io.github.juc211.band_schedule.domain.InputLinkType;
import io.github.juc211.band_schedule.repository.ClubRepository;
import io.github.juc211.band_schedule.repository.InputLinkRepository;
import io.github.juc211.band_schedule.repository.PerformanceRepository;
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
class AdminSecurityControllerTest {

	private static final String MASTER_TOKEN = "test-master-token";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ClubRepository clubRepository;

	@Autowired
	private PerformanceRepository performanceRepository;

	@Autowired
	private InputLinkRepository inputLinkRepository;

	@Test
	void createClubRejectsMissingMasterAdminToken() throws Exception {
		mockMvc.perform(post("/api/admin/clubs")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "Crane"
								}
								"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("A001"));
	}

	@Test
	void createClubWithMasterAdminTokenReturnsAdminToken() throws Exception {
		String response = createClub("Crane");
		Long clubId = extractLong(response, "clubId");
		String adminToken = extractString(response, "adminToken");
		String adminUrl = extractString(response, "adminUrl");

		assertThat(clubId).isPositive();
		assertThat(adminToken).isNotBlank();
		assertThat(adminUrl).startsWith("/admin/clubs/");
	}

	@Test
	void clubAdminCanCreatePerformanceInOwnClub() throws Exception {
		String clubToken = createClubToken("Crane");

		mockMvc.perform(post("/api/admin/clubs/current/performances")
						.header("X-Club-Admin-Token", clubToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "2026 Summer Concert",
								  "performanceDate": "2026-08-20",
								  "location": "Main Hall"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.performanceId").isNumber());
	}

	@Test
	void clubAdminCannotUpdateAnotherClubsPerformance() throws Exception {
		String clubAToken = createClubToken("Club A");
		String clubBToken = createClubToken("Club B");
		Long clubBPerformanceId = createPerformance(clubBToken);

		mockMvc.perform(patch("/api/performances/{performanceId}", clubBPerformanceId)
						.header("X-Club-Admin-Token", clubAToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "Blocked Update",
								  "performanceDate": "2026-08-21",
								  "location": "Other Hall"
								}
								"""))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("A003"));
	}

	@Test
	void inputLinkTokenCannotCallAdminApi() throws Exception {
		String clubToken = createClubToken("Crane");
		Long performanceId = createPerformance(clubToken);
		InputLink inputLink = inputLinkRepository.save(InputLink.create("song-request-token", performanceRepository.findById(performanceId).orElseThrow(), InputLinkType.SONG_REQUEST, true, null));

		mockMvc.perform(patch("/api/performances/{performanceId}", performanceId)
						.header("X-Club-Admin-Token", inputLink.getToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "Blocked Update",
								  "performanceDate": "2026-08-21",
								  "location": "Other Hall"
								}
								"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("A002"));
	}

	@Test
	void persistedClubAdminTokenCanBeResolvedAgain() throws Exception {
		String clubToken = createClubToken("Crane");

		mockMvc.perform(get("/api/admin/clubs/current")
						.header("X-Club-Admin-Token", clubToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.adminToken").value(clubToken));

		assertThat(clubRepository.findByAdminToken(clubToken)).isPresent();
	}

	private String createClub(String name) throws Exception {
		return mockMvc.perform(post("/api/admin/clubs")
						.header("X-Master-Admin-Token", MASTER_TOKEN)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "%s"
								}
								""".formatted(name)))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();
	}

	private String createClubToken(String name) throws Exception {
		return extractString(createClub(name), "adminToken");
	}

	private Long createPerformance(String clubToken) throws Exception {
		String response = mockMvc.perform(post("/api/admin/clubs/current/performances")
						.header("X-Club-Admin-Token", clubToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "2026 Summer Concert",
								  "performanceDate": "2026-08-20",
								  "location": "Main Hall"
								}
								"""))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();
		return extractLong(response, "performanceId");
	}

	private Long extractLong(String json, String fieldName) {
		return Long.valueOf(json.replaceAll(".*\\\"" + fieldName + "\\\":(\\d+).*", "$1"));
	}

	private String extractString(String json, String fieldName) {
		return json.replaceAll(".*\\\"" + fieldName + "\\\":\\\"([^\\\"]+)\\\".*", "$1");
	}
}
