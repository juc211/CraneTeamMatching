package io.github.juc211.band_schedule.service;

import io.github.juc211.band_schedule.domain.PerformanceConfirmedSong;
import io.github.juc211.band_schedule.domain.Performance;
import io.github.juc211.band_schedule.dto.PerformanceConfirmedSongDto;
import io.github.juc211.band_schedule.repository.PerformanceConfirmedSongRepository;
import io.github.juc211.band_schedule.repository.PerformanceRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PerformanceConfirmedSongService {

	private final PerformanceConfirmedSongRepository performanceConfirmedSongRepository;
	private final PerformanceRepository performanceRepository;

	/**
	 * 공연 단위 확정곡 생성
	 */
	public PerformanceConfirmedSongDto.PerformanceConfirmedSongResponse createPerformanceConfirmedSong(
			Long performanceId,
			PerformanceConfirmedSongDto.PerformanceConfirmedSongCreateRequest request
	) {
		Performance performance = performanceRepository.findById(performanceId)
				.orElseThrow(() -> new IllegalArgumentException("Performance not found: " + performanceId));

		PerformanceConfirmedSong savedPerformanceConfirmedSong = performanceConfirmedSongRepository.save(PerformanceConfirmedSong.create(performance, request.song()));

		return toPerformanceConfirmedSongResponse(savedPerformanceConfirmedSong);
	}

	/**
	 * 공연 단위 확정곡 목록 조회
	 */
	@Transactional(readOnly = true)
	public List<PerformanceConfirmedSongDto.PerformanceConfirmedSongResponse> getPerformanceConfirmedSongsByPerformance(Long performanceId) {
		validatePerformanceExists(performanceId);

		return performanceConfirmedSongRepository.findByPerformanceIdOrderByIdAsc(performanceId)
				.stream()
				.map(this::toPerformanceConfirmedSongResponse)
				.toList();
	}

	/**
	 * 공연 단위 확정곡 수정
	 */
	public PerformanceConfirmedSongDto.PerformanceConfirmedSongResponse updatePerformanceConfirmedSong(
			Long performanceConfirmedSongId,
			PerformanceConfirmedSongDto.PerformanceConfirmedSongUpdateRequest request
	) {
		PerformanceConfirmedSong performanceConfirmedSong = performanceConfirmedSongRepository.findById(performanceConfirmedSongId)
				.orElseThrow(() -> new IllegalArgumentException("PerformanceConfirmedSong not found: " + performanceConfirmedSongId));

		performanceConfirmedSong.update(request.song());

		return toPerformanceConfirmedSongResponse(performanceConfirmedSong);
	}

	/**
	 * 공연 단위 확정곡 삭제
	 */
	public void deletePerformanceConfirmedSong(Long performanceConfirmedSongId) {
		PerformanceConfirmedSong performanceConfirmedSong = performanceConfirmedSongRepository.findById(performanceConfirmedSongId)
				.orElseThrow(() -> new IllegalArgumentException("PerformanceConfirmedSong not found: " + performanceConfirmedSongId));

		performanceConfirmedSongRepository.delete(performanceConfirmedSong);
	}

	private void validatePerformanceExists(Long performanceId) {
		if (!performanceRepository.existsById(performanceId)) {
			throw new IllegalArgumentException("Performance not found: " + performanceId);
		}
	}

	private PerformanceConfirmedSongDto.PerformanceConfirmedSongResponse toPerformanceConfirmedSongResponse(PerformanceConfirmedSong performanceConfirmedSong) {
		return new PerformanceConfirmedSongDto.PerformanceConfirmedSongResponse(
				performanceConfirmedSong.getId(),
				performanceConfirmedSong.getPerformance().getId(),
				performanceConfirmedSong.getSong(),
				performanceConfirmedSong.getCreatedAt()
		);
	}
}
