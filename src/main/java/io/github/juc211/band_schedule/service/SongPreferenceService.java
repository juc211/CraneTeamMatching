package io.github.juc211.band_schedule.service;

import io.github.juc211.band_schedule.domain.InputLink;
import io.github.juc211.band_schedule.domain.InputLinkType;
import io.github.juc211.band_schedule.domain.Performance;
import io.github.juc211.band_schedule.domain.PerformanceConfirmedSong;
import io.github.juc211.band_schedule.domain.PerformanceMember;
import io.github.juc211.band_schedule.domain.SongPreference;
import io.github.juc211.band_schedule.dto.SongPreferenceDto;
import io.github.juc211.band_schedule.repository.InputLinkRepository;
import io.github.juc211.band_schedule.repository.PerformanceConfirmedSongRepository;
import io.github.juc211.band_schedule.repository.PerformanceMemberRepository;
import io.github.juc211.band_schedule.repository.PerformanceRepository;
import io.github.juc211.band_schedule.repository.SongPreferenceRepository;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class SongPreferenceService {

	private final SongPreferenceRepository songPreferenceRepository;
	private final InputLinkRepository inputLinkRepository;
	private final PerformanceRepository performanceRepository;
	private final PerformanceConfirmedSongRepository performanceConfirmedSongRepository;
	private final PerformanceMemberRepository performanceMemberRepository;

	/**
	 * 링크 기반 확정곡 선호도 제출 및 전체 교체
	 */
	public List<SongPreferenceDto.SongPreferenceResponse> submitSongPreferences(
			String token,
			SongPreferenceDto.SongPreferenceSubmitRequest request
	) {
		InputLink inputLink = inputLinkRepository.findByToken(token)
				.orElseThrow(() -> new IllegalArgumentException("InputLink not found: " + token));
		PerformanceMember performanceMember = performanceMemberRepository.findById(request.performanceMemberId())
				.orElseThrow(() -> new IllegalArgumentException("PerformanceMember not found: " + request.performanceMemberId()));

		validateUsableLink(inputLink);
		validateLinkType(inputLink, InputLinkType.SONG_PREFERENCE);
		validatePerformanceMemberBelongsToLinkPerformance(inputLink, performanceMember);

		List<SongPreferenceDto.SongPreferenceItemRequest> preferences = request.preferences() == null
				? List.of()
				: request.preferences();
		List<SongPreference> songPreferences = validateAndCreateSongPreferences(inputLink.getPerformance(), performanceMember, preferences);
		validateAllConfirmedSongsAreSubmitted(inputLink.getPerformance().getId(), preferences);

		songPreferenceRepository.deleteByPerformanceMemberIdAndPerformanceConfirmedSongPerformanceId(
				performanceMember.getId(),
				inputLink.getPerformance().getId()
		);
		songPreferenceRepository.saveAll(songPreferences);

		return songPreferenceRepository
				.findByPerformanceMemberIdAndPerformanceConfirmedSongPerformanceIdOrderByPerformanceConfirmedSongIdAsc(
						performanceMember.getId(),
						inputLink.getPerformance().getId()
				)
				.stream()
				.map(this::toSongPreferenceResponse)
				.toList();
	}

	/**
	 * 공연 참여 인원별 확정곡 선호도 조회
	 */
	@Transactional(readOnly = true)
	public List<SongPreferenceDto.SongPreferenceResponse> getSongPreferencesByPerformanceMember(
			Long performanceId,
			Long performanceMemberId
	) {
		validatePerformanceExists(performanceId);
		PerformanceMember performanceMember = performanceMemberRepository.findById(performanceMemberId)
				.orElseThrow(() -> new IllegalArgumentException("PerformanceMember not found: " + performanceMemberId));
		validatePerformanceMemberBelongsToPerformance(performanceId, performanceMember);

		return songPreferenceRepository
				.findByPerformanceMemberIdAndPerformanceConfirmedSongPerformanceIdOrderByPerformanceConfirmedSongIdAsc(performanceMemberId, performanceId)
				.stream()
				.map(this::toSongPreferenceResponse)
				.toList();
	}

	/**
	 * 공연 확정곡 선호도 결과 조회
	 */
	@Transactional(readOnly = true)
	public List<SongPreferenceDto.SongPreferenceResultResponse> getSongPreferenceResults(Long performanceId) {
		validatePerformanceExists(performanceId);

		List<PerformanceConfirmedSong> confirmedSongs = performanceConfirmedSongRepository.findByPerformanceIdOrderByIdAsc(performanceId);
		List<SongPreference> preferences = songPreferenceRepository
				.findByPerformanceConfirmedSongPerformanceIdOrderByPerformanceConfirmedSongIdAscRankAscIdAsc(performanceId);
		Map<Long, List<SongPreference>> preferencesBySongId = preferences.stream()
				.collect(
						LinkedHashMap::new,
						(map, preference) -> map.computeIfAbsent(preference.getPerformanceConfirmedSong().getId(), key -> new java.util.ArrayList<>()).add(preference),
						Map::putAll
				);

		return confirmedSongs.stream()
				.map(confirmedSong -> toSongPreferenceResultResponse(
						confirmedSong,
						preferencesBySongId.getOrDefault(confirmedSong.getId(), List.of())
				))
				.toList();
	}

	/**
	 * 선호도 응답 삭제
	 */
	public void deleteSongPreference(Long songPreferenceId) {
		SongPreference songPreference = songPreferenceRepository.findById(songPreferenceId)
				.orElseThrow(() -> new IllegalArgumentException("SongPreference not found: " + songPreferenceId));

		songPreferenceRepository.delete(songPreference);
	}

	/**
	 * 선호도 요청 검증 후 엔티티 생성
	 */
	private List<SongPreference> validateAndCreateSongPreferences(
			Performance performance,
			PerformanceMember performanceMember,
			List<SongPreferenceDto.SongPreferenceItemRequest> preferences
	) {
		Set<Long> confirmedSongIds = new HashSet<>();
		return preferences.stream()
				.map(preference -> {
					validateSongPreferenceItemRequest(preference, confirmedSongIds);
					PerformanceConfirmedSong confirmedSong = performanceConfirmedSongRepository.findById(preference.performanceConfirmedSongId())
							.orElseThrow(() -> new IllegalArgumentException("PerformanceConfirmedSong not found: " + preference.performanceConfirmedSongId()));
					validateConfirmedSongBelongsToPerformance(performance, confirmedSong);
					return SongPreference.create(confirmedSong, performanceMember, preference.rank());
				})
				.toList();
	}

	/**
	 * 공연의 모든 확정곡 선호도 제출 여부 검증
	 */
	private void validateAllConfirmedSongsAreSubmitted(
			Long performanceId,
			List<SongPreferenceDto.SongPreferenceItemRequest> preferences
	) {
		Set<Long> requiredConfirmedSongIds = performanceConfirmedSongRepository.findByPerformanceIdOrderByIdAsc(performanceId)
				.stream()
				.map(PerformanceConfirmedSong::getId)
				.collect(Collectors.toSet());
		Set<Long> submittedConfirmedSongIds = preferences.stream()
				.map(SongPreferenceDto.SongPreferenceItemRequest::performanceConfirmedSongId)
				.collect(Collectors.toSet());

		if (!submittedConfirmedSongIds.equals(requiredConfirmedSongIds)) {
			throw new IllegalArgumentException("Song preference must be submitted for all performance confirmed songs");
		}
	}

	/**
	 * 선호도 요청 값 검증
	 */
	private void validateSongPreferenceItemRequest(
			SongPreferenceDto.SongPreferenceItemRequest preference,
			Set<Long> confirmedSongIds
	) {
		if (preference == null) {
			throw new IllegalArgumentException("Song preference item is required");
		}
		if (preference.performanceConfirmedSongId() == null) {
			throw new IllegalArgumentException("PerformanceConfirmedSong id is required");
		}
		if (preference.rank() == null || preference.rank() < 1) {
			throw new IllegalArgumentException("Song preference rank must be positive");
		}
		if (!confirmedSongIds.add(preference.performanceConfirmedSongId())) {
			throw new IllegalArgumentException("Song preference cannot contain duplicate confirmed song");
		}
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
	 * 공연 참여 인원이 링크 공연에 속하는지 검증
	 */
	private void validatePerformanceMemberBelongsToLinkPerformance(InputLink inputLink, PerformanceMember performanceMember) {
		validatePerformanceMemberBelongsToPerformance(inputLink.getPerformance().getId(), performanceMember);
	}

	/**
	 * 공연 참여 인원이 공연에 속하는지 검증
	 */
	private void validatePerformanceMemberBelongsToPerformance(Long performanceId, PerformanceMember performanceMember) {
		if (!performanceMember.getPerformance().getId().equals(performanceId)) {
			throw new IllegalArgumentException("PerformanceMember does not belong to performance");
		}
	}

	/**
	 * 확정곡이 공연에 속하는지 검증
	 */
	private void validateConfirmedSongBelongsToPerformance(Performance performance, PerformanceConfirmedSong confirmedSong) {
		if (!confirmedSong.getPerformance().getId().equals(performance.getId())) {
			throw new IllegalArgumentException("PerformanceConfirmedSong does not belong to performance");
		}
	}

	/**
	 * 선호도 결과 응답 변환
	 */
	private SongPreferenceDto.SongPreferenceResultResponse toSongPreferenceResultResponse(
			PerformanceConfirmedSong confirmedSong,
			List<SongPreference> preferences
	) {
		List<SongPreferenceDto.SongPreferenceResponse> preferenceResponses = preferences.stream()
				.map(this::toSongPreferenceResponse)
				.toList();
		Double averageRank = preferences.isEmpty()
				? null
				: preferences.stream()
						.mapToInt(SongPreference::getRank)
						.average()
						.orElseThrow();

		return new SongPreferenceDto.SongPreferenceResultResponse(
				confirmedSong.getId(),
				confirmedSong.getPerformance().getId(),
				confirmedSong.getSong(),
				preferences.size(),
				averageRank,
				preferenceResponses
		);
	}

	/**
	 * 선호도 응답 변환
	 */
	private SongPreferenceDto.SongPreferenceResponse toSongPreferenceResponse(SongPreference songPreference) {
		PerformanceConfirmedSong confirmedSong = songPreference.getPerformanceConfirmedSong();
		PerformanceMember performanceMember = songPreference.getPerformanceMember();
		return new SongPreferenceDto.SongPreferenceResponse(
				songPreference.getId(),
				confirmedSong.getId(),
				confirmedSong.getPerformance().getId(),
				confirmedSong.getSong(),
				performanceMember.getId(),
				performanceMember.getUser().getId(),
				performanceMember.getUser().getName(),
				songPreference.getRank()
		);
	}
}
