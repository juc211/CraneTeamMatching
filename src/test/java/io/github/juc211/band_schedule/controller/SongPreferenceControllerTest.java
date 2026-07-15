package io.github.juc211.band_schedule.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.juc211.band_schedule.domain.InputLink;
import io.github.juc211.band_schedule.domain.InputLinkType;
import io.github.juc211.band_schedule.domain.Performance;
import io.github.juc211.band_schedule.domain.PerformanceConfirmedSong;
import io.github.juc211.band_schedule.domain.PerformanceMember;
import io.github.juc211.band_schedule.domain.SongPreference;
import io.github.juc211.band_schedule.domain.User;
import io.github.juc211.band_schedule.repository.InputLinkRepository;
import io.github.juc211.band_schedule.repository.PerformanceConfirmedSongRepository;
import io.github.juc211.band_schedule.repository.PerformanceMemberRepository;
import io.github.juc211.band_schedule.repository.PerformanceRepository;
import io.github.juc211.band_schedule.repository.SongPreferenceRepository;
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
class SongPreferenceControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private SongPreferenceRepository songPreferenceRepository;

	@Autowired
	private InputLinkRepository inputLinkRepository;

	@Autowired
	private PerformanceRepository performanceRepository;

	@Autowired
	private PerformanceConfirmedSongRepository performanceConfirmedSongRepository;

	@Autowired
	private PerformanceMemberRepository performanceMemberRepository;

	@Autowired
	private UserRepository userRepository;

	@Test
	void submitSongPreferencesReturnsOkStatus() throws Exception {
		Performance performance = createPerformance();
		InputLink inputLink = inputLinkRepository.save(InputLink.create("preference-token", performance, InputLinkType.SONG_PREFERENCE, true, null));
		PerformanceMember performanceMember = createPerformanceMember(performance, "김보컬", "20260001");
		PerformanceConfirmedSong firstSong = performanceConfirmedSongRepository.save(PerformanceConfirmedSong.create(performance, "Song A"));
		PerformanceConfirmedSong secondSong = performanceConfirmedSongRepository.save(PerformanceConfirmedSong.create(performance, "Song B"));

		mockMvc.perform(put("/api/song-preferences/{token}", inputLink.getToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "performanceMemberId": %d,
								  "preferences": [
								    { "performanceConfirmedSongId": %d, "rank": 1 },
								    { "performanceConfirmedSongId": %d, "rank": 1 }
								  ]
								}
								""".formatted(performanceMember.getId(), firstSong.getId(), secondSong.getId())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].song").value("Song A"))
				.andExpect(jsonPath("$[0].rank").value(1))
				.andExpect(jsonPath("$[1].song").value("Song B"))
				.andExpect(jsonPath("$[1].rank").value(1));
	}

	@Test
	void getSongPreferenceResultsReturnsOkStatus() throws Exception {
		Performance performance = createPerformance();
		PerformanceMember performanceMember = createPerformanceMember(performance, "김보컬", "20260001");
		PerformanceConfirmedSong confirmedSong = performanceConfirmedSongRepository.save(PerformanceConfirmedSong.create(performance, "Song A", "김보컬 추천"));
		songPreferenceRepository.save(SongPreference.create(confirmedSong, performanceMember, 2));

		mockMvc.perform(get("/api/performances/{performanceId}/song-preferences/results", performance.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].song").value("Song A"))
				.andExpect(jsonPath("$[0].adminMemo").value("김보컬 추천"))
				.andExpect(jsonPath("$[0].preferenceCount").value(1))
				.andExpect(jsonPath("$[0].averageRank").value(2.0))
				.andExpect(jsonPath("$[0].preferences[0].userName").value("김보컬"));
	}

	@Test
	void getSongPreferencesByPerformanceMemberReturnsOkStatus() throws Exception {
		Performance performance = createPerformance();
		PerformanceMember performanceMember = createPerformanceMember(performance, "김보컬", "20260001");
		PerformanceConfirmedSong confirmedSong = performanceConfirmedSongRepository.save(PerformanceConfirmedSong.create(performance, "Song A"));
		songPreferenceRepository.save(SongPreference.create(confirmedSong, performanceMember, 2));

		mockMvc.perform(get(
						"/api/performances/{performanceId}/performance-members/{performanceMemberId}/song-preferences",
						performance.getId(),
						performanceMember.getId()
				))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].song").value("Song A"))
				.andExpect(jsonPath("$[0].rank").value(2));
	}

	@Test
	void deleteSongPreferenceReturnsNoContentStatus() throws Exception {
		Performance performance = createPerformance();
		PerformanceMember performanceMember = createPerformanceMember(performance, "김보컬", "20260001");
		PerformanceConfirmedSong confirmedSong = performanceConfirmedSongRepository.save(PerformanceConfirmedSong.create(performance, "Song A"));
		SongPreference songPreference = songPreferenceRepository.save(SongPreference.create(confirmedSong, performanceMember, 2));

		mockMvc.perform(delete("/api/song-preferences/{songPreferenceId}", songPreference.getId()))
				.andExpect(status().isNoContent());
	}

	private Performance createPerformance() {
		return performanceRepository.save(Performance.create(
				"2026 Summer Concert",
				LocalDate.of(2026, 8, 20),
				"Main Hall"
		));
	}

	private PerformanceMember createPerformanceMember(Performance performance, String name, String studentNumber) {
		User user = userRepository.save(User.create(name, studentNumber));
		return performanceMemberRepository.save(PerformanceMember.create(performance, user));
	}
}
