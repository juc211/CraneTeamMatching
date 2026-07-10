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
import io.github.juc211.band_schedule.domain.PerformanceMember;
import io.github.juc211.band_schedule.domain.SongRequest;
import io.github.juc211.band_schedule.domain.SongVote;
import io.github.juc211.band_schedule.domain.Team;
import io.github.juc211.band_schedule.domain.User;
import io.github.juc211.band_schedule.domain.Vote;
import io.github.juc211.band_schedule.repository.InputLinkRepository;
import io.github.juc211.band_schedule.repository.PerformanceMemberRepository;
import io.github.juc211.band_schedule.repository.PerformanceRepository;
import io.github.juc211.band_schedule.repository.SongRequestRepository;
import io.github.juc211.band_schedule.repository.SongVoteRepository;
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
class SongControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private InputLinkRepository inputLinkRepository;

	@Autowired
	private PerformanceRepository performanceRepository;

	@Autowired
	private PerformanceMemberRepository performanceMemberRepository;

	@Autowired
	private SongRequestRepository songRequestRepository;

	@Autowired
	private SongVoteRepository songVoteRepository;

	@Autowired
	private TeamRepository teamRepository;

	@Autowired
	private UserRepository userRepository;

	@Test
	void createSongRequestWithoutSelectedTeamReturnsCreatedStatus() throws Exception {
		Performance performance = performanceRepository.save(
				Performance.create("2026 Summer Concert", LocalDate.of(2026, 8, 15), "Main Hall")
		);
		User user = userRepository.save(User.create("Kim Band", "20261234"));
		PerformanceMember performanceMember = performanceMemberRepository.save(PerformanceMember.create(performance, user));
		InputLink inputLink = inputLinkRepository.save(
				InputLink.create("performance-token", performance, true, LocalDateTime.now().plusDays(1))
		);

		mockMvc.perform(post("/api/song-requests/{token}", inputLink.getToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "teamId": null,
								  "requestedByMemberId": %d,
								  "song": "Song A - Artist A",
								  "youtubeUrl": "https://www.youtube.com/watch?v=abc123"
								}
								""".formatted(performanceMember.getId())))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.songRequestId").isNumber())
				.andExpect(jsonPath("$.performanceId").value(performance.getId()))
				.andExpect(jsonPath("$.teamId").doesNotExist())
				.andExpect(jsonPath("$.requestedByMemberId").value(performanceMember.getId()))
				.andExpect(jsonPath("$.song").value("Song A - Artist A"))
				.andExpect(jsonPath("$.youtubeUrl").value("https://www.youtube.com/watch?v=abc123"));
	}

	@Test
	void createSongRequestWithSelectedTeamReturnsCreatedStatus() throws Exception {
		Performance performance = performanceRepository.save(
				Performance.create("2026 Summer Concert", LocalDate.of(2026, 8, 15), "Main Hall")
		);
		Team team = teamRepository.save(Team.create(performance, "Team A", "Confirmed Song"));
		User user = userRepository.save(User.create("Kim Band", "20261234"));
		PerformanceMember performanceMember = performanceMemberRepository.save(PerformanceMember.create(performance, user));
		InputLink inputLink = inputLinkRepository.save(
				InputLink.create("performance-token", performance, true, LocalDateTime.now().plusDays(1))
		);

		mockMvc.perform(post("/api/song-requests/{token}", inputLink.getToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "teamId": %d,
								  "requestedByMemberId": %d,
								  "song": "Song B - Artist B"
								}
								""".formatted(team.getId(), performanceMember.getId())))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.songRequestId").isNumber())
				.andExpect(jsonPath("$.performanceId").value(performance.getId()))
				.andExpect(jsonPath("$.teamId").value(team.getId()))
				.andExpect(jsonPath("$.requestedByMemberId").value(performanceMember.getId()))
				.andExpect(jsonPath("$.song").value("Song B - Artist B"));
	}

	@Test
	void getSongRequestsByPerformanceReturnsOkStatus() throws Exception {
		Performance performance = performanceRepository.save(
				Performance.create("2026 Summer Concert", LocalDate.of(2026, 8, 15), "Main Hall")
		);
		Team team = teamRepository.save(Team.create(performance, "Team A", "Confirmed Song"));
		User user = userRepository.save(User.create("Kim Band", "20261234"));
		PerformanceMember performanceMember = performanceMemberRepository.save(PerformanceMember.create(performance, user));
		songRequestRepository.save(SongRequest.create(performance, null, performanceMember, "Performance Song - Artist A"));
		songRequestRepository.save(SongRequest.create(performance, team, performanceMember, "Team Song - Artist B"));

		mockMvc.perform(get("/api/performances/{performanceId}/song-requests", performance.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].song").value("Performance Song - Artist A"))
				.andExpect(jsonPath("$[1].song").value("Team Song - Artist B"));
	}

	@Test
	void getSongRequestsByTeamReturnsOkStatus() throws Exception {
		Performance performance = performanceRepository.save(
				Performance.create("2026 Summer Concert", LocalDate.of(2026, 8, 15), "Main Hall")
		);
		Team firstTeam = teamRepository.save(Team.create(performance, "Team A", "Confirmed Song A"));
		Team secondTeam = teamRepository.save(Team.create(performance, "Team B", "Confirmed Song B"));
		User user = userRepository.save(User.create("Kim Band", "20261234"));
		PerformanceMember performanceMember = performanceMemberRepository.save(PerformanceMember.create(performance, user));
		songRequestRepository.save(SongRequest.create(performance, firstTeam, performanceMember, "First Team Song - Artist A"));
		songRequestRepository.save(SongRequest.create(performance, secondTeam, performanceMember, "Second Team Song - Artist B"));

		mockMvc.perform(get("/api/teams/{teamId}/song-requests", firstTeam.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].song").value("First Team Song - Artist A"))
				.andExpect(jsonPath("$[1]").doesNotExist());
	}

	@Test
	void updateSongRequestReturnsOkStatus() throws Exception {
		Performance performance = performanceRepository.save(
				Performance.create("2026 Summer Concert", LocalDate.of(2026, 8, 15), "Main Hall")
		);
		Team team = teamRepository.save(Team.create(performance, "Team A", "Confirmed Song"));
		User user = userRepository.save(User.create("Kim Band", "20261234"));
		PerformanceMember performanceMember = performanceMemberRepository.save(PerformanceMember.create(performance, user));
		SongRequest songRequest = songRequestRepository.save(
				SongRequest.create(performance, null, performanceMember, "Before Song - Artist A")
		);

		mockMvc.perform(patch("/api/song-requests/{songRequestId}", songRequest.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "teamId": %d,
								  "song": "After Song - Artist B",
								  "youtubeUrl": "https://youtu.be/updated"
								}
								""".formatted(team.getId())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.songRequestId").value(songRequest.getId()))
				.andExpect(jsonPath("$.performanceId").value(performance.getId()))
				.andExpect(jsonPath("$.teamId").value(team.getId()))
				.andExpect(jsonPath("$.requestedByMemberId").value(performanceMember.getId()))
				.andExpect(jsonPath("$.song").value("After Song - Artist B"))
				.andExpect(jsonPath("$.youtubeUrl").value("https://youtu.be/updated"));
	}

	@Test
	void deleteSongRequestReturnsNoContentStatus() throws Exception {
		Performance performance = performanceRepository.save(
				Performance.create("2026 Summer Concert", LocalDate.of(2026, 8, 15), "Main Hall")
		);
		User user = userRepository.save(User.create("Kim Band", "20261234"));
		PerformanceMember performanceMember = performanceMemberRepository.save(PerformanceMember.create(performance, user));
		SongRequest songRequest = songRequestRepository.save(
				SongRequest.create(performance, null, performanceMember, "Song A - Artist A")
		);

		mockMvc.perform(delete("/api/song-requests/{songRequestId}", songRequest.getId()))
				.andExpect(status().isNoContent());
	}

	@Test
	void submitSongVoteReturnsCreatedStatus() throws Exception {
		Performance performance = performanceRepository.save(
				Performance.create("2026 Summer Concert", LocalDate.of(2026, 8, 15), "Main Hall")
		);
		User requesterUser = userRepository.save(User.create("Kim Requester", "20261234"));
		User voterUser = userRepository.save(User.create("Lee Voter", "20261235"));
		PerformanceMember requesterMember = performanceMemberRepository.save(PerformanceMember.create(performance, requesterUser));
		PerformanceMember voterMember = performanceMemberRepository.save(PerformanceMember.create(performance, voterUser));
		InputLink inputLink = inputLinkRepository.save(
				InputLink.create("vote-token", performance, InputLinkType.SONG_VOTE, true, LocalDateTime.now().plusDays(1))
		);
		SongRequest songRequest = songRequestRepository.save(
				SongRequest.create(performance, null, requesterMember, "Song A - Artist A")
		);

		mockMvc.perform(post("/api/song-vote/{token}", inputLink.getToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "songRequestId": %d,
								  "voterMemberId": %d,
								  "vote": "POSSIBLE",
								  "reason": "가능"
								}
								""".formatted(songRequest.getId(), voterMember.getId())))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.songVoteId").isNumber())
				.andExpect(jsonPath("$.songRequestId").value(songRequest.getId()))
				.andExpect(jsonPath("$.voterMemberId").value(voterMember.getId()))
				.andExpect(jsonPath("$.vote").value(Vote.POSSIBLE.name()))
				.andExpect(jsonPath("$.reason").value("가능"));
	}

	@Test
	void getSongVotesBySongRequestReturnsOkStatus() throws Exception {
		Performance performance = performanceRepository.save(
				Performance.create("2026 Summer Concert", LocalDate.of(2026, 8, 15), "Main Hall")
		);
		User user = userRepository.save(User.create("Kim Band", "20261234"));
		PerformanceMember performanceMember = performanceMemberRepository.save(PerformanceMember.create(performance, user));
		SongRequest songRequest = songRequestRepository.save(
				SongRequest.create(performance, null, performanceMember, "Song A - Artist A")
		);
		SongVote songVote = songVoteRepository.save(SongVote.create(songRequest, performanceMember, Vote.POSSIBLE, "가능"));

		mockMvc.perform(get("/api/song-requests/{songRequestId}/votes", songRequest.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].songVoteId").value(songVote.getId()))
				.andExpect(jsonPath("$[0].songRequestId").value(songRequest.getId()))
				.andExpect(jsonPath("$[0].voterMemberId").value(performanceMember.getId()))
				.andExpect(jsonPath("$[0].vote").value(Vote.POSSIBLE.name()))
				.andExpect(jsonPath("$[0].reason").value("가능"));
	}

	@Test
	void deleteSongVoteReturnsNoContentStatus() throws Exception {
		Performance performance = performanceRepository.save(
				Performance.create("2026 Summer Concert", LocalDate.of(2026, 8, 15), "Main Hall")
		);
		User user = userRepository.save(User.create("Kim Band", "20261234"));
		PerformanceMember performanceMember = performanceMemberRepository.save(PerformanceMember.create(performance, user));
		SongRequest songRequest = songRequestRepository.save(
				SongRequest.create(performance, null, performanceMember, "Song A - Artist A")
		);
		SongVote songVote = songVoteRepository.save(SongVote.create(songRequest, performanceMember, Vote.POSSIBLE, "가능"));

		mockMvc.perform(delete("/api/song-votes/{songVoteId}", songVote.getId()))
				.andExpect(status().isNoContent());
	}

}
