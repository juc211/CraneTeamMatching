package io.github.juc211.band_schedule.service;

import io.github.juc211.band_schedule.domain.InputLink;
import io.github.juc211.band_schedule.domain.InputLinkType;
import io.github.juc211.band_schedule.domain.PerformanceConfirmedSong;
import io.github.juc211.band_schedule.domain.Performance;
import io.github.juc211.band_schedule.dto.PerformanceConfirmedSongDto;
import io.github.juc211.band_schedule.repository.InputLinkRepository;
import io.github.juc211.band_schedule.repository.PerformanceConfirmedSongRepository;
import io.github.juc211.band_schedule.repository.PerformanceRepository;
import io.github.juc211.band_schedule.repository.SongPreferenceRepository;
import java.time.LocalDateTime;
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
	private final InputLinkRepository inputLinkRepository;
	private final SongPreferenceRepository songPreferenceRepository;

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
	 * 링크 기반 공연 단위 확정곡 목록 조회
	 */
	@Transactional(readOnly = true)
	public List<PerformanceConfirmedSongDto.PerformanceConfirmedSongResponse> getPerformanceConfirmedSongsByLink(String token) {
		InputLink inputLink = inputLinkRepository.findByToken(token)
				.orElseThrow(() -> new IllegalArgumentException("InputLink not found: " + token));

		validateUsableLink(inputLink);
		validateLinkType(inputLink, InputLinkType.SONG_PREFERENCE);

		return performanceConfirmedSongRepository.findByPerformanceIdOrderByIdAsc(inputLink.getPerformance().getId())
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

		songPreferenceRepository.deleteByPerformanceConfirmedSongId(performanceConfirmedSongId);
		performanceConfirmedSongRepository.delete(performanceConfirmedSong);
	}

	/**
	 * 링크 사용 가능 여부 검증
	 */
	private void validateUsableLink(InputLink inputLink) {
		if (!inputLink.isActive()) {
			throw new IllegalArgumentException("InputLink is inactive");
		}
		if (inputLink.getExpiresAt() != null && inputLink.getExpiresAt().isBefore(LocalDateTime.now())) {
			throw new IllegalArgumentException("InputLink is expired");
		}
	}

	/**
	 * 링크 타입 검증
	 */
	private void validateLinkType(InputLink inputLink, InputLinkType expectedType) {
		if (inputLink.getType() != expectedType) {
			throw new IllegalArgumentException("InputLink type must be " + expectedType);
		}
	}

	/**
	 * 공연 존재 여부 검증
	 */
	private void validatePerformanceExists(Long performanceId) {
		if (!performanceRepository.existsById(performanceId)) {
			throw new IllegalArgumentException("Performance not found: " + performanceId);
		}
	}

	/**
	 * 공연 단위 확정곡 응답 변환
	 */
	private PerformanceConfirmedSongDto.PerformanceConfirmedSongResponse toPerformanceConfirmedSongResponse(PerformanceConfirmedSong performanceConfirmedSong) {
		return new PerformanceConfirmedSongDto.PerformanceConfirmedSongResponse(
				performanceConfirmedSong.getId(),
				performanceConfirmedSong.getPerformance().getId(),
				performanceConfirmedSong.getSong(),
				performanceConfirmedSong.getCreatedAt()
		);
	}
}
