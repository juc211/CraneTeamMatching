package io.github.juc211.band_schedule.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.juc211.band_schedule.domain.InputLink;
import io.github.juc211.band_schedule.domain.InputLinkType;
import io.github.juc211.band_schedule.domain.Performance;
import io.github.juc211.band_schedule.domain.PerformanceMember;
import io.github.juc211.band_schedule.domain.SongRequest;
import io.github.juc211.band_schedule.domain.SongVote;
import io.github.juc211.band_schedule.domain.Team;
import io.github.juc211.band_schedule.domain.User;
import io.github.juc211.band_schedule.domain.Vote;
import io.github.juc211.band_schedule.dto.SongDto;
import io.github.juc211.band_schedule.exception.BusinessException;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class SongServiceTest {

	@Autowired
	private SongService songService;

	@Autowired
	private SongRequestRepository songRequestRepository;

	@Autowired
	private SongVoteRepository songVoteRepository;

	@Autowired
	private InputLinkRepository inputLinkRepository;

	@Autowired
	private PerformanceRepository performanceRepository;

	@Autowired
	private PerformanceMemberRepository performanceMemberRepository;

	@Autowired
	private TeamRepository teamRepository;

	@Autowired
	private UserRepository userRepository;

	@Test
	void createSongRequestWithoutSelectedTeamStoresNullTeam() {
		Performance performance = performanceRepository.save(
				Performance.create("2026 Summer Concert", LocalDate.of(2026, 8, 15), "Main Hall")
		);
		User user = userRepository.save(User.create("Kim Band", "20261234"));
		PerformanceMember performanceMember = performanceMemberRepository.save(PerformanceMember.create(performance, user));
		InputLink inputLink = inputLinkRepository.save(
				InputLink.create("performance-token", performance, true, LocalDateTime.now().plusDays(1))
		);

		SongDto.SongRequestResponse response = songService.createSongRequest(
				inputLink.getToken(),
				new SongDto.SongRequestCreateRequest(null, performanceMember.getId(), "Song A - Artist A")
		);

		SongRequest savedSongRequest = songRequestRepository.findById(response.songRequestId()).orElseThrow();
		assertThat(response.performanceId()).isEqualTo(performance.getId());
		assertThat(response.teamId()).isNull();
		assertThat(response.requestedByMemberId()).isEqualTo(performanceMember.getId());
		assertThat(response.song()).isEqualTo("Song A - Artist A");
		assertThat(response.youtubeUrl()).isNull();
		assertThat(savedSongRequest.getTeam()).isNull();
		assertThat(savedSongRequest.getCreatedAt()).isNotNull();
	}

	@Test
	void createSongRequestStoresYoutubeUrl() {
		Performance performance = performanceRepository.save(
				Performance.create("2026 Summer Concert", LocalDate.of(2026, 8, 15), "Main Hall")
		);
		User user = userRepository.save(User.create("Kim Band", "20261234"));
		PerformanceMember performanceMember = performanceMemberRepository.save(PerformanceMember.create(performance, user));
		InputLink inputLink = inputLinkRepository.save(
				InputLink.create("performance-token", performance, true, LocalDateTime.now().plusDays(1))
		);

		SongDto.SongRequestResponse response = songService.createSongRequest(
				inputLink.getToken(),
				new SongDto.SongRequestCreateRequest(
						null,
						performanceMember.getId(),
						"Song A - Artist A",
						"https://www.youtube.com/watch?v=abc123"
				)
		);

		SongRequest savedSongRequest = songRequestRepository.findById(response.songRequestId()).orElseThrow();
		assertThat(response.youtubeUrl()).isEqualTo("https://www.youtube.com/watch?v=abc123");
		assertThat(savedSongRequest.getYoutubeUrl()).isEqualTo("https://www.youtube.com/watch?v=abc123");
	}

	@Test
	void createSongRequestWithSelectedTeamStoresTeam() {
		Performance performance = performanceRepository.save(
				Performance.create("2026 Summer Concert", LocalDate.of(2026, 8, 15), "Main Hall")
		);
		Team team = teamRepository.save(Team.create(performance, "Team A", "Confirmed Song"));
		User user = userRepository.save(User.create("Kim Band", "20261234"));
		PerformanceMember performanceMember = performanceMemberRepository.save(PerformanceMember.create(performance, user));
		InputLink inputLink = inputLinkRepository.save(
				InputLink.create("performance-token", performance, true, LocalDateTime.now().plusDays(1))
		);

		SongDto.SongRequestResponse response = songService.createSongRequest(
				inputLink.getToken(),
				new SongDto.SongRequestCreateRequest(team.getId(), performanceMember.getId(), "Song B - Artist B")
		);

		assertThat(response.performanceId()).isEqualTo(performance.getId());
		assertThat(response.teamId()).isEqualTo(team.getId());
		assertThat(response.requestedByMemberId()).isEqualTo(performanceMember.getId());
		assertThat(response.song()).isEqualTo("Song B - Artist B");
	}

	@Test
	void createSongRequestRejectsExpiredSongRequestLink() {
		Performance performance = performanceRepository.save(
				Performance.create("2026 Summer Concert", LocalDate.of(2026, 8, 15), "Main Hall")
		);
		User user = userRepository.save(User.create("Kim Band", "20261234"));
		PerformanceMember performanceMember = performanceMemberRepository.save(PerformanceMember.create(performance, user));
		InputLink inputLink = inputLinkRepository.save(
				InputLink.create(
						"expired-song-request-token",
						performance,
						InputLinkType.SONG_REQUEST,
						true,
						LocalDateTime.now().minusMinutes(1)
				)
		);

		assertThatThrownBy(() -> songService.createSongRequest(
				inputLink.getToken(),
				new SongDto.SongRequestCreateRequest(null, performanceMember.getId(), "Song A - Artist A")
		))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("InputLink is expired");
	}

	@Test
	void getSongRequestsByPerformanceReturnsAllSongRequestsInPerformance() {
		Performance performance = performanceRepository.save(
				Performance.create("2026 Summer Concert", LocalDate.of(2026, 8, 15), "Main Hall")
		);
		Team team = teamRepository.save(Team.create(performance, "Team A", "Confirmed Song"));
		User user = userRepository.save(User.create("Kim Band", "20261234"));
		PerformanceMember performanceMember = performanceMemberRepository.save(PerformanceMember.create(performance, user));
		songRequestRepository.save(SongRequest.create(performance, null, performanceMember, "Performance Song - Artist A"));
		songRequestRepository.save(SongRequest.create(performance, team, performanceMember, "Team Song - Artist B"));

		assertThat(songService.getSongRequestsByPerformance(performance.getId()))
				.extracting(SongDto.SongRequestResponse::song)
				.containsExactly("Performance Song - Artist A", "Team Song - Artist B");
	}

	@Test
	void getSongRequestsByTeamReturnsOnlySelectedTeamSongRequests() {
		Performance performance = performanceRepository.save(
				Performance.create("2026 Summer Concert", LocalDate.of(2026, 8, 15), "Main Hall")
		);
		Team firstTeam = teamRepository.save(Team.create(performance, "Team A", "Confirmed Song A"));
		Team secondTeam = teamRepository.save(Team.create(performance, "Team B", "Confirmed Song B"));
		User user = userRepository.save(User.create("Kim Band", "20261234"));
		PerformanceMember performanceMember = performanceMemberRepository.save(PerformanceMember.create(performance, user));
		songRequestRepository.save(SongRequest.create(performance, firstTeam, performanceMember, "First Team Song - Artist A"));
		songRequestRepository.save(SongRequest.create(performance, secondTeam, performanceMember, "Second Team Song - Artist B"));

		assertThat(songService.getSongRequestsByTeam(firstTeam.getId()))
				.extracting(SongDto.SongRequestResponse::song)
				.containsExactly("First Team Song - Artist A");
	}

	@Test
	void updateSongRequestChangesTeamAndSong() {
		Performance performance = performanceRepository.save(
				Performance.create("2026 Summer Concert", LocalDate.of(2026, 8, 15), "Main Hall")
		);
		Team team = teamRepository.save(Team.create(performance, "Team A", "Confirmed Song"));
		User user = userRepository.save(User.create("Kim Band", "20261234"));
		PerformanceMember performanceMember = performanceMemberRepository.save(PerformanceMember.create(performance, user));
		SongRequest songRequest = songRequestRepository.save(
				SongRequest.create(performance, null, performanceMember, "Before Song - Artist A")
		);

		SongDto.SongRequestResponse response = songService.updateSongRequest(
				songRequest.getId(),
				new SongDto.SongRequestUpdateRequest(
						team.getId(),
						"After Song - Artist B",
						"https://youtu.be/updated"
				)
		);

		SongRequest updatedSongRequest = songRequestRepository.findById(songRequest.getId()).orElseThrow();
		assertThat(response.songRequestId()).isEqualTo(songRequest.getId());
		assertThat(response.teamId()).isEqualTo(team.getId());
		assertThat(response.song()).isEqualTo("After Song - Artist B");
		assertThat(response.youtubeUrl()).isEqualTo("https://youtu.be/updated");
		assertThat(updatedSongRequest.getTeam().getId()).isEqualTo(team.getId());
		assertThat(updatedSongRequest.getSong()).isEqualTo("After Song - Artist B");
		assertThat(updatedSongRequest.getYoutubeUrl()).isEqualTo("https://youtu.be/updated");
	}

	@Test
	void updateSongRequestRejectsTeamFromDifferentPerformance() {
		Performance firstPerformance = performanceRepository.save(
				Performance.create("2026 Summer Concert", LocalDate.of(2026, 8, 15), "Main Hall")
		);
		Performance secondPerformance = performanceRepository.save(
				Performance.create("2026 Winter Concert", LocalDate.of(2026, 12, 20), "Club Room")
		);
		Team otherTeam = teamRepository.save(Team.create(secondPerformance, "Team B", "Confirmed Song"));
		User user = userRepository.save(User.create("Kim Band", "20261234"));
		PerformanceMember performanceMember = performanceMemberRepository.save(PerformanceMember.create(firstPerformance, user));
		SongRequest songRequest = songRequestRepository.save(
				SongRequest.create(firstPerformance, null, performanceMember, "Before Song - Artist A")
		);

		assertThatThrownBy(() -> songService.updateSongRequest(
				songRequest.getId(),
				new SongDto.SongRequestUpdateRequest(otherTeam.getId(), "After Song - Artist B")
		))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("Team does not belong to song request performance");
	}

	@Test
	void deleteSongRequestRemovesSongRequest() {
		Performance performance = performanceRepository.save(
				Performance.create("2026 Summer Concert", LocalDate.of(2026, 8, 15), "Main Hall")
		);
		User user = userRepository.save(User.create("Kim Band", "20261234"));
		PerformanceMember performanceMember = performanceMemberRepository.save(PerformanceMember.create(performance, user));
		SongRequest songRequest = songRequestRepository.save(
				SongRequest.create(performance, null, performanceMember, "Song A - Artist A")
		);

		songService.deleteSongRequest(songRequest.getId());

		assertThat(songRequestRepository.findById(songRequest.getId())).isEmpty();
	}

	@Test
	void deleteSongRequestRemovesSongVotesTogether() {
		Performance performance = performanceRepository.save(
				Performance.create("2026 Summer Concert", LocalDate.of(2026, 8, 15), "Main Hall")
		);
		User user = userRepository.save(User.create("Kim Band", "20261234"));
		PerformanceMember performanceMember = performanceMemberRepository.save(PerformanceMember.create(performance, user));
		SongRequest songRequest = songRequestRepository.save(
				SongRequest.create(performance, null, performanceMember, "Song A - Artist A")
		);
		SongVote songVote = songVoteRepository.save(SongVote.create(songRequest, performanceMember, Vote.POSSIBLE, "가능"));

		songService.deleteSongRequest(songRequest.getId());

		assertThat(songVoteRepository.findById(songVote.getId())).isEmpty();
		assertThat(songRequestRepository.findById(songRequest.getId())).isEmpty();
	}

	@Test
	void deleteSongVoteRemovesSongVote() {
		Performance performance = performanceRepository.save(
				Performance.create("2026 Summer Concert", LocalDate.of(2026, 8, 15), "Main Hall")
		);
		User user = userRepository.save(User.create("Kim Band", "20261234"));
		PerformanceMember performanceMember = performanceMemberRepository.save(PerformanceMember.create(performance, user));
		SongRequest songRequest = songRequestRepository.save(
				SongRequest.create(performance, null, performanceMember, "Song A - Artist A")
		);
		SongVote songVote = songVoteRepository.save(SongVote.create(songRequest, performanceMember, Vote.POSSIBLE, "가능"));

		songService.deleteSongVote(songVote.getId());

		assertThat(songVoteRepository.findById(songVote.getId())).isEmpty();
		assertThat(songRequestRepository.findById(songRequest.getId())).isPresent();
	}

	@Test
	void submitSongVoteAllowsVotingOwnAndOthersSongRequests() {
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
		SongRequest ownSongRequest = songRequestRepository.save(
				SongRequest.create(performance, null, voterMember, "Own Song - Artist A")
		);
		SongRequest othersSongRequest = songRequestRepository.save(
				SongRequest.create(performance, null, requesterMember, "Others Song - Artist B")
		);

		SongDto.SongVoteResponse ownVoteResponse = songService.submitSongVote(
				inputLink.getToken(),
				new SongDto.SongVoteSubmitRequest(ownSongRequest.getId(), voterMember.getId(), Vote.POSSIBLE, "가능")
		);
		SongDto.SongVoteResponse othersVoteResponse = songService.submitSongVote(
				inputLink.getToken(),
				new SongDto.SongVoteSubmitRequest(othersSongRequest.getId(), voterMember.getId(), Vote.HOLD, "보류")
		);

		assertThat(songVoteRepository.findAll()).hasSize(2);
		assertThat(ownVoteResponse.songRequestId()).isEqualTo(ownSongRequest.getId());
		assertThat(ownVoteResponse.voterMemberId()).isEqualTo(voterMember.getId());
		assertThat(ownVoteResponse.vote()).isEqualTo(Vote.POSSIBLE);
		assertThat(othersVoteResponse.songRequestId()).isEqualTo(othersSongRequest.getId());
		assertThat(othersVoteResponse.vote()).isEqualTo(Vote.HOLD);
	}

	@Test
	void submitSongVoteUpdatesExistingVoteForSameSongRequestAndVoter() {
		Performance performance = performanceRepository.save(
				Performance.create("2026 Summer Concert", LocalDate.of(2026, 8, 15), "Main Hall")
		);
		User user = userRepository.save(User.create("Kim Band", "20261234"));
		PerformanceMember voterMember = performanceMemberRepository.save(PerformanceMember.create(performance, user));
		InputLink inputLink = inputLinkRepository.save(
				InputLink.create("vote-token", performance, InputLinkType.SONG_VOTE, true, LocalDateTime.now().plusDays(1))
		);
		SongRequest songRequest = songRequestRepository.save(
				SongRequest.create(performance, null, voterMember, "Song A - Artist A")
		);

		songService.submitSongVote(
				inputLink.getToken(),
				new SongDto.SongVoteSubmitRequest(songRequest.getId(), voterMember.getId(), Vote.POSSIBLE, "가능")
		);
		SongDto.SongVoteResponse response = songService.submitSongVote(
				inputLink.getToken(),
				new SongDto.SongVoteSubmitRequest(songRequest.getId(), voterMember.getId(), Vote.IMPOSSIBLE, "불가능")
		);

		assertThat(songVoteRepository.findAll()).hasSize(1);
		SongVote savedSongVote = songVoteRepository.findById(response.songVoteId()).orElseThrow();
		assertThat(savedSongVote.getVote()).isEqualTo(Vote.IMPOSSIBLE);
		assertThat(savedSongVote.getReason()).isEqualTo("불가능");
	}

	@Test
	void getSongVotesBySongRequestReturnsVotesForSongRequest() {
		Performance performance = performanceRepository.save(
				Performance.create("2026 Summer Concert", LocalDate.of(2026, 8, 15), "Main Hall")
		);
		User firstUser = userRepository.save(User.create("Kim Vocal", "20261234"));
		User secondUser = userRepository.save(User.create("Lee Guitar", "20261235"));
		PerformanceMember firstMember = performanceMemberRepository.save(PerformanceMember.create(performance, firstUser));
		PerformanceMember secondMember = performanceMemberRepository.save(PerformanceMember.create(performance, secondUser));
		SongRequest songRequest = songRequestRepository.save(
				SongRequest.create(performance, null, firstMember, "Song A - Artist A")
		);
		songVoteRepository.save(SongVote.create(songRequest, firstMember, Vote.POSSIBLE, "가능"));
		songVoteRepository.save(SongVote.create(songRequest, secondMember, Vote.HOLD, "보류"));

		assertThat(songService.getSongVotesBySongRequest(songRequest.getId()))
				.extracting(SongDto.SongVoteResponse::vote)
				.containsExactly(Vote.POSSIBLE, Vote.HOLD);
	}

	@Test
	void submitSongVoteRejectsSongRequestFromDifferentPerformance() {
		Performance firstPerformance = performanceRepository.save(
				Performance.create("2026 Summer Concert", LocalDate.of(2026, 8, 15), "Main Hall")
		);
		Performance secondPerformance = performanceRepository.save(
				Performance.create("2026 Winter Concert", LocalDate.of(2026, 12, 20), "Club Room")
		);
		User user = userRepository.save(User.create("Kim Band", "20261234"));
		PerformanceMember voterMember = performanceMemberRepository.save(PerformanceMember.create(firstPerformance, user));
		InputLink inputLink = inputLinkRepository.save(
				InputLink.create("vote-token", firstPerformance, InputLinkType.SONG_VOTE, true, LocalDateTime.now().plusDays(1))
		);
		SongRequest otherPerformanceSongRequest = songRequestRepository.save(
				SongRequest.create(secondPerformance, null, voterMember, "Song A - Artist A")
		);

		assertThatThrownBy(() -> songService.submitSongVote(
				inputLink.getToken(),
				new SongDto.SongVoteSubmitRequest(otherPerformanceSongRequest.getId(), voterMember.getId(), Vote.POSSIBLE, "가능")
		))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("SongRequest does not belong to link performance");
	}

	@Test
	void createSongRequestRejectsSelectedTeamFromDifferentPerformance() {
		Performance firstPerformance = performanceRepository.save(
				Performance.create("2026 Summer Concert", LocalDate.of(2026, 8, 15), "Main Hall")
		);
		Performance secondPerformance = performanceRepository.save(
				Performance.create("2026 Winter Concert", LocalDate.of(2026, 12, 20), "Club Room")
		);
		Team otherTeam = teamRepository.save(Team.create(secondPerformance, "Team B", "Confirmed Song"));
		User user = userRepository.save(User.create("Kim Band", "20261234"));
		PerformanceMember performanceMember = performanceMemberRepository.save(PerformanceMember.create(firstPerformance, user));
		InputLink inputLink = inputLinkRepository.save(
				InputLink.create("performance-token", firstPerformance, true, LocalDateTime.now().plusDays(1))
		);

		assertThatThrownBy(() -> songService.createSongRequest(
				inputLink.getToken(),
				new SongDto.SongRequestCreateRequest(otherTeam.getId(), performanceMember.getId(), "Song A - Artist A")
		))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("Team does not belong to link performance");
	}

	@Test
	void createSongRequestRejectsMemberFromDifferentPerformance() {
		Performance firstPerformance = performanceRepository.save(
				Performance.create("2026 Summer Concert", LocalDate.of(2026, 8, 15), "Main Hall")
		);
		Performance secondPerformance = performanceRepository.save(
				Performance.create("2026 Winter Concert", LocalDate.of(2026, 12, 20), "Club Room")
		);
		User user = userRepository.save(User.create("Kim Band", "20261234"));
		PerformanceMember otherPerformanceMember = performanceMemberRepository.save(PerformanceMember.create(secondPerformance, user));
		InputLink inputLink = inputLinkRepository.save(
				InputLink.create("performance-token", firstPerformance, true, LocalDateTime.now().plusDays(1))
		);

		assertThatThrownBy(() -> songService.createSongRequest(
				inputLink.getToken(),
				new SongDto.SongRequestCreateRequest(null, otherPerformanceMember.getId(), "Song A - Artist A")
		))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("PerformanceMember does not belong to link performance");
	}
}
