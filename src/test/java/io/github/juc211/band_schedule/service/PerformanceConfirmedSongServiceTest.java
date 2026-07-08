package io.github.juc211.band_schedule.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.juc211.band_schedule.domain.PerformanceConfirmedSong;
import io.github.juc211.band_schedule.domain.Performance;
import io.github.juc211.band_schedule.dto.PerformanceConfirmedSongDto;
import io.github.juc211.band_schedule.repository.PerformanceConfirmedSongRepository;
import io.github.juc211.band_schedule.repository.PerformanceRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class PerformanceConfirmedSongServiceTest {

	@Autowired
	private PerformanceConfirmedSongService performanceConfirmedSongService;

	@Autowired
	private PerformanceConfirmedSongRepository performanceConfirmedSongRepository;

	@Autowired
	private PerformanceRepository performanceRepository;

	@Test
	void createPerformanceConfirmedSongPersistsPerformanceConfirmedSongInPerformance() {
		Performance performance = performanceRepository.save(
				Performance.create("2026 Summer Concert", LocalDate.of(2026, 8, 15), "Main Hall")
		);

		PerformanceConfirmedSongDto.PerformanceConfirmedSongResponse response = performanceConfirmedSongService.createPerformanceConfirmedSong(
				performance.getId(),
				new PerformanceConfirmedSongDto.PerformanceConfirmedSongCreateRequest("Confirmed Song - Artist A")
		);

		PerformanceConfirmedSong savedPerformanceConfirmedSong = performanceConfirmedSongRepository.findById(response.performanceConfirmedSongId()).orElseThrow();
		assertThat(response.performanceId()).isEqualTo(performance.getId());
		assertThat(response.song()).isEqualTo("Confirmed Song - Artist A");
		assertThat(savedPerformanceConfirmedSong.getPerformance().getId()).isEqualTo(performance.getId());
		assertThat(savedPerformanceConfirmedSong.getCreatedAt()).isNotNull();
	}

	@Test
	void getPerformanceConfirmedSongsByPerformanceReturnsPerformanceConfirmedSongsInPerformance() {
		Performance performance = performanceRepository.save(
				Performance.create("2026 Summer Concert", LocalDate.of(2026, 8, 15), "Main Hall")
		);
		performanceConfirmedSongRepository.save(PerformanceConfirmedSong.create(performance, "Confirmed Song A - Artist A"));
		performanceConfirmedSongRepository.save(PerformanceConfirmedSong.create(performance, "Confirmed Song B - Artist B"));

		assertThat(performanceConfirmedSongService.getPerformanceConfirmedSongsByPerformance(performance.getId()))
				.extracting(PerformanceConfirmedSongDto.PerformanceConfirmedSongResponse::song)
				.containsExactly("Confirmed Song A - Artist A", "Confirmed Song B - Artist B");
	}

	@Test
	void deletePerformanceConfirmedSongRemovesPerformanceConfirmedSong() {
		Performance performance = performanceRepository.save(
				Performance.create("2026 Summer Concert", LocalDate.of(2026, 8, 15), "Main Hall")
		);
		PerformanceConfirmedSong confirmedSong = performanceConfirmedSongRepository.save(
				PerformanceConfirmedSong.create(performance, "Confirmed Song - Artist A")
		);

		performanceConfirmedSongService.deletePerformanceConfirmedSong(confirmedSong.getId());

		assertThat(performanceConfirmedSongRepository.findById(confirmedSong.getId())).isEmpty();
	}
}
