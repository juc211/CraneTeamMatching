package io.github.juc211.band_schedule.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void createUserReturnsCreatedStatus() throws Exception {
		mockMvc.perform(post("/api/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "Kim Band",
								  "studentNumber": "20261234"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.userId").isNumber())
				.andExpect(jsonPath("$.name").value("Kim Band"))
				.andExpect(jsonPath("$.studentNumber").value("20261234"));
	}

	@Test
	void getUsersReturnsOkStatus() throws Exception {
		mockMvc.perform(post("/api/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "Kim Band",
								  "studentNumber": "20261234"
								}
								"""))
				.andExpect(status().isCreated());

		mockMvc.perform(get("/api/users"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].name").value("Kim Band"))
				.andExpect(jsonPath("$[0].studentNumber").value("20261234"));
	}

	@Test
	void getUserReturnsOkStatus() throws Exception {
		String createResponse = mockMvc.perform(post("/api/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "Kim Band",
								  "studentNumber": "20261234"
								}
								"""))
				.andReturn()
				.getResponse()
				.getContentAsString();
		Long userId = Long.valueOf(createResponse.replaceAll(".*\\\"userId\\\":(\\d+).*", "$1"));

		mockMvc.perform(get("/api/users/{userId}", userId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.userId").value(userId))
				.andExpect(jsonPath("$.name").value("Kim Band"))
				.andExpect(jsonPath("$.studentNumber").value("20261234"));
	}

	@Test
	void updateUserReturnsOkStatus() throws Exception {
		String createResponse = mockMvc.perform(post("/api/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "Kim Band",
								  "studentNumber": "20261234"
								}
								"""))
				.andReturn()
				.getResponse()
				.getContentAsString();
		Long userId = Long.valueOf(createResponse.replaceAll(".*\\\"userId\\\":(\\d+).*", "$1"));

		mockMvc.perform(patch("/api/users/{userId}", userId)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "Kim Vocal",
								  "studentNumber": "20269999"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.userId").value(userId))
				.andExpect(jsonPath("$.name").value("Kim Vocal"))
				.andExpect(jsonPath("$.studentNumber").value("20269999"));
	}
}
