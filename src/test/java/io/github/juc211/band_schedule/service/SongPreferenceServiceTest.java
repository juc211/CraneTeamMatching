package io.github.juc211.band_schedule.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.juc211.band_schedule.domain.InputLink;
import io.github.juc211.band_schedule.domain.InputLinkType;
import io.github.juc211.band_schedule.domain.Performance;
import io.github.juc211.band_schedule.domain.PerformanceConfirmedSong;
import io.github.juc211.band_schedule.domain.PerformanceMember;
import io.github.juc211.band_schedule.domain.SongPreference;
import io.github.juc211.band_schedule.domain.User;
import io.github.juc211.band_schedule.dto.SongPreferenceDto;
import io.github.juc211.band_schedule.exception.BusinessException;
import io.github.juc211.band_schedule.repository.InputLinkRepository;
import io.github.juc211.band_schedule.repository.PerformanceConfirmedSongRepository;
import io.github.juc211.band_schedule.repository.PerformanceMemberRepository;
import io.github.juc211.band_schedule.repository.PerformanceRepository;
import io.github.juc211.band_schedule.repository.SongPreferenceRepository;
import io.github.juc211.band_schedule.repository.UserRepository;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class SongPreferenceServiceTest {

	@Autowired
	private SongPreferenceService songPreferenceService;

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
	void submitSongPreferencesAllowsDuplicateRanks() {
		Performance performance = createPerformance();
		InputLink inputLink = inputLinkRepository.save(InputLink.create("preference-token", performance, InputLinkType.SONG_PREFERENCE, true, null));
		PerformanceMember performanceMember = createPerformanceMember(performance, "김보컬", "20260001");
		PerformanceConfirmedSong firstSong = performanceConfirmedSongRepository.save(PerformanceConfirmedSong.create(performance, "Song A"));
		PerformanceConfirmedSong secondSong = performanceConfirmedSongRepository.save(PerformanceConfirmedSong.create(performance, "Song B"));

		List<SongPreferenceDto.SongPreferenceResponse> responses = songPreferenceService.submitSongPreferences(
				inputLink.getToken(),
				new SongPreferenceDto.SongPreferenceSubmitRequest(
						performanceMember.getId(),
						List.of(
								new SongPreferenceDto.SongPreferenceItemRequest(firstSong.getId(), 1),
								new SongPreferenceDto.SongPreferenceItemRequest(secondSong.getId(), 1)
						)
				)
		);

		assertThat(responses)
				.extracting(SongPreferenceDto.SongPreferenceResponse::rank)
				.containsExactly(1, 1);
		assertThat(songPreferenceRepository.findAll()).hasSize(2);
	}

	@Test
	void submitSongPreferencesReplacesPreviousPreferencesForMember() {
		Performance performance = createPerformance();
		InputLink inputLink = inputLinkRepository.save(InputLink.create("preference-token", performance, InputLinkType.SONG_PREFERENCE, true, null));
		PerformanceMember performanceMember = createPerformanceMember(performance, "김보컬", "20260001");
		PerformanceConfirmedSong firstSong = performanceConfirmedSongRepository.save(PerformanceConfirmedSong.create(performance, "Song A"));
		PerformanceConfirmedSong secondSong = performanceConfirmedSongRepository.save(PerformanceConfirmedSong.create(performance, "Song B"));
		songPreferenceRepository.save(SongPreference.create(firstSong, performanceMember, 1));

		songPreferenceService.submitSongPreferences(
				inputLink.getToken(),
				new SongPreferenceDto.SongPreferenceSubmitRequest(
						performanceMember.getId(),
						List.of(
								new SongPreferenceDto.SongPreferenceItemRequest(firstSong.getId(), 3),
								new SongPreferenceDto.SongPreferenceItemRequest(secondSong.getId(), 2)
						)
				)
		);

		assertThat(songPreferenceService.getSongPreferencesByPerformanceMember(performance.getId(), performanceMember.getId()))
				.extracting(SongPreferenceDto.SongPreferenceResponse::song)
				.containsExactly("Song A", "Song B");
	}

	@Test
	void submitSongPreferencesRejectsWhenNotAllConfirmedSongsAreSubmitted() {
		Performance performance = createPerformance();
		InputLink inputLink = inputLinkRepository.save(InputLink.create("preference-token", performance, InputLinkType.SONG_PREFERENCE, true, null));
		PerformanceMember performanceMember = createPerformanceMember(performance, "김보컬", "20260001");
		PerformanceConfirmedSong firstSong = performanceConfirmedSongRepository.save(PerformanceConfirmedSong.create(performance, "Song A"));
		performanceConfirmedSongRepository.save(PerformanceConfirmedSong.create(performance, "Song B"));

		assertThatThrownBy(() -> songPreferenceService.submitSongPreferences(
				inputLink.getToken(),
				new SongPreferenceDto.SongPreferenceSubmitRequest(
						performanceMember.getId(),
						List.of(new SongPreferenceDto.SongPreferenceItemRequest(firstSong.getId(), 1))
				)
		))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("Song preference must be submitted for all performance confirmed songs");
	}

	@Test
	void submitSongPreferencesRejectsConfirmedSongFromDifferentPerformance() {
		Performance performance = createPerformance();
		Performance otherPerformance = createPerformance();
		InputLink inputLink = inputLinkRepository.save(InputLink.create("preference-token", performance, InputLinkType.SONG_PREFERENCE, true, null));
		PerformanceMember performanceMember = createPerformanceMember(performance, "김보컬", "20260001");
		PerformanceConfirmedSong otherSong = performanceConfirmedSongRepository.save(PerformanceConfirmedSong.create(otherPerformance, "Other Song"));

		assertThatThrownBy(() -> songPreferenceService.submitSongPreferences(
				inputLink.getToken(),
				new SongPreferenceDto.SongPreferenceSubmitRequest(
						performanceMember.getId(),
						List.of(new SongPreferenceDto.SongPreferenceItemRequest(otherSong.getId(), 1))
				)
		))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("PerformanceConfirmedSong does not belong to performance");
	}

	@Test
	void submitSongPreferencesRejectsDuplicateConfirmedSong() {
		Performance performance = createPerformance();
		InputLink inputLink = inputLinkRepository.save(InputLink.create("preference-token", performance, InputLinkType.SONG_PREFERENCE, true, null));
		PerformanceMember performanceMember = createPerformanceMember(performance, "김보컬", "20260001");
		PerformanceConfirmedSong confirmedSong = performanceConfirmedSongRepository.save(PerformanceConfirmedSong.create(performance, "Song A"));

		assertThatThrownBy(() -> songPreferenceService.submitSongPreferences(
				inputLink.getToken(),
				new SongPreferenceDto.SongPreferenceSubmitRequest(
						performanceMember.getId(),
						List.of(
								new SongPreferenceDto.SongPreferenceItemRequest(confirmedSong.getId(), 1),
								new SongPreferenceDto.SongPreferenceItemRequest(confirmedSong.getId(), 2)
						)
				)
		))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("Song preference cannot contain duplicate confirmed song");
	}

	@Test
	void submitSongPreferencesRejectsNullPreferenceItem() {
		Performance performance = createPerformance();
		InputLink inputLink = inputLinkRepository.save(InputLink.create("preference-token", performance, InputLinkType.SONG_PREFERENCE, true, null));
		PerformanceMember performanceMember = createPerformanceMember(performance, "김보컬", "20260001");
		performanceConfirmedSongRepository.save(PerformanceConfirmedSong.create(performance, "Song A"));

		assertThatThrownBy(() -> songPreferenceService.submitSongPreferences(
				inputLink.getToken(),
				new SongPreferenceDto.SongPreferenceSubmitRequest(
						performanceMember.getId(),
						Collections.singletonList(null)
				)
		))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("Song preference item is required");
	}

	@Test
	void getSongPreferenceResultsReturnsAverageRankByConfirmedSong() {
		Performance performance = createPerformance();
		PerformanceMember firstMember = createPerformanceMember(performance, "김보컬", "20260001");
		PerformanceMember secondMember = createPerformanceMember(performance, "최기타", "20260002");
		PerformanceConfirmedSong confirmedSong = performanceConfirmedSongRepository.save(PerformanceConfirmedSong.create(performance, "Song A"));
		songPreferenceRepository.save(SongPreference.create(confirmedSong, firstMember, 1));
		songPreferenceRepository.save(SongPreference.create(confirmedSong, secondMember, 3));

		List<SongPreferenceDto.SongPreferenceResultResponse> responses = songPreferenceService.getSongPreferenceResults(performance.getId());

		assertThat(responses).hasSize(1);
		assertThat(responses.get(0).preferenceCount()).isEqualTo(2);
		assertThat(responses.get(0).averageRank()).isEqualTo(2.0);
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
