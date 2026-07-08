package io.github.juc211.band_schedule.service;

import io.github.juc211.band_schedule.domain.FinalSchedule;
import io.github.juc211.band_schedule.domain.InputLink;
import io.github.juc211.band_schedule.domain.InputLinkType;
import io.github.juc211.band_schedule.domain.Team;
import io.github.juc211.band_schedule.dto.FinalScheduleDto;
import io.github.juc211.band_schedule.repository.FinalScheduleRepository;
import io.github.juc211.band_schedule.repository.InputLinkRepository;
import io.github.juc211.band_schedule.repository.PerformanceRepository;
import io.github.juc211.band_schedule.repository.TeamRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class FinalScheduleService {

	private final FinalScheduleRepository finalScheduleRepository;
	private final TeamRepository teamRepository;
	private final PerformanceRepository performanceRepository;
	private final InputLinkRepository inputLinkRepository;

	/**
	 * 팀 최종 합주 일정 생성
	 */
	public FinalScheduleDto.FinalScheduleResponse createFinalSchedule(
			Long teamId,
			FinalScheduleDto.FinalScheduleCreateRequest request
	) {
		Team team = teamRepository.findById(teamId)
				.orElseThrow(() -> new IllegalArgumentException("Team not found: " + teamId));

		validateNoOverlap(request.startDateTime(), request.endDateTime(), null);

		FinalSchedule savedFinalSchedule = finalScheduleRepository.save(
				FinalSchedule.create(team, request.startDateTime(), request.endDateTime(), request.memo())
		);

		return toFinalScheduleResponse(savedFinalSchedule);
	}

	/**
	 * 팀 최종 합주 일정 목록 조회
	 */
	@Transactional(readOnly = true)
	public List<FinalScheduleDto.FinalScheduleResponse> getFinalSchedulesByTeam(Long teamId) {
		validateTeamExists(teamId);

		return finalScheduleRepository.findByTeamIdOrderByStartDateTimeAscIdAsc(teamId)
				.stream()
				.map(this::toFinalScheduleResponse)
				.toList();
	}

	/**
	 * 공연 전체 최종 합주 일정 목록 조회
	 */
	@Transactional(readOnly = true)
	public List<FinalScheduleDto.FinalScheduleResponse> getFinalSchedulesByPerformance(Long performanceId) {
		validatePerformanceExists(performanceId);

		return finalScheduleRepository.findByTeamPerformanceIdOrderByStartDateTimeAscIdAsc(performanceId)
				.stream()
				.map(this::toFinalScheduleResponse)
				.toList();
	}

	/**
	 * 링크 기반 공연 전체 최종 합주 일정 조회
	 */
	@Transactional(readOnly = true)
	public List<FinalScheduleDto.FinalScheduleResponse> getFinalSchedulesByLink(String token) {
		InputLink inputLink = getUsableFinalScheduleViewLink(token);

		return finalScheduleRepository.findByTeamPerformanceIdOrderByStartDateTimeAscIdAsc(inputLink.getPerformance().getId())
				.stream()
				.map(this::toFinalScheduleResponse)
				.toList();
	}

	/**
	 * 링크 기반 특정 팀 최종 합주 일정 조회
	 */
	@Transactional(readOnly = true)
	public List<FinalScheduleDto.FinalScheduleResponse> getFinalSchedulesByLinkAndTeam(String token, Long teamId) {
		InputLink inputLink = getUsableFinalScheduleViewLink(token);
		Team team = teamRepository.findById(teamId)
				.orElseThrow(() -> new IllegalArgumentException("Team not found: " + teamId));

		validateTeamBelongsToLinkPerformance(inputLink, team);

		return finalScheduleRepository.findByTeamIdOrderByStartDateTimeAscIdAsc(teamId)
				.stream()
				.map(this::toFinalScheduleResponse)
				.toList();
	}

	/**
	 * 최종 합주 일정 수정
	 */
	public FinalScheduleDto.FinalScheduleResponse updateFinalSchedule(
			Long finalScheduleId,
			FinalScheduleDto.FinalScheduleUpdateRequest request
	) {
		FinalSchedule finalSchedule = finalScheduleRepository.findById(finalScheduleId)
				.orElseThrow(() -> new IllegalArgumentException("FinalSchedule not found: " + finalScheduleId));

		validateNoOverlap(request.startDateTime(), request.endDateTime(), finalSchedule.getId());

		finalSchedule.update(request.startDateTime(), request.endDateTime(), request.memo());

		return toFinalScheduleResponse(finalSchedule);
	}

	/**
	 * 최종 합주 일정 삭제
	 */
	public void deleteFinalSchedule(Long finalScheduleId) {
		FinalSchedule finalSchedule = finalScheduleRepository.findById(finalScheduleId)
				.orElseThrow(() -> new IllegalArgumentException("FinalSchedule not found: " + finalScheduleId));

		finalScheduleRepository.delete(finalSchedule);
	}

	private void validateNoOverlap(
			LocalDateTime startDateTime,
			LocalDateTime endDateTime,
			Long excludedFinalScheduleId
	) {
		if (startDateTime == null || endDateTime == null) {
			throw new IllegalArgumentException("Final schedule start and end date time must be set together");
		}
		if (!startDateTime.isBefore(endDateTime)) {
			throw new IllegalArgumentException("Final schedule start date time must be before end date time");
		}

		boolean hasOverlap = finalScheduleRepository
				.findByStartDateTimeLessThanAndEndDateTimeGreaterThan(
						endDateTime,
						startDateTime
				)
				.stream()
				.anyMatch(finalSchedule -> !finalSchedule.getId().equals(excludedFinalScheduleId));

		if (hasOverlap) {
			throw new IllegalArgumentException("Final schedule overlaps with another final schedule");
		}
	}

	private InputLink getUsableFinalScheduleViewLink(String token) {
		InputLink inputLink = inputLinkRepository.findByToken(token)
				.orElseThrow(() -> new IllegalArgumentException("InputLink not found: " + token));
		validateUsableLink(inputLink);
		if (inputLink.getType() != InputLinkType.FINAL_SCHEDULE_VIEW) {
			throw new IllegalArgumentException("InputLink type must be " + InputLinkType.FINAL_SCHEDULE_VIEW);
		}
		return inputLink;
	}

	private void validateUsableLink(InputLink inputLink) {
		if (!inputLink.isActive()) {
			throw new IllegalArgumentException("InputLink is inactive");
		}
		if (inputLink.getExpiresAt() != null && inputLink.getExpiresAt().isBefore(LocalDateTime.now())) {
			throw new IllegalArgumentException("InputLink is expired");
		}
	}

	private void validateTeamBelongsToLinkPerformance(InputLink inputLink, Team team) {
		Long linkPerformanceId = inputLink.getPerformance().getId();
		Long teamPerformanceId = team.getPerformance().getId();
		if (!linkPerformanceId.equals(teamPerformanceId)) {
			throw new IllegalArgumentException("Team does not belong to link performance");
		}
	}

	private void validateTeamExists(Long teamId) {
		if (!teamRepository.existsById(teamId)) {
			throw new IllegalArgumentException("Team not found: " + teamId);
		}
	}

	private void validatePerformanceExists(Long performanceId) {
		if (!performanceRepository.existsById(performanceId)) {
			throw new IllegalArgumentException("Performance not found: " + performanceId);
		}
	}

	private FinalScheduleDto.FinalScheduleResponse toFinalScheduleResponse(FinalSchedule finalSchedule) {
		Team team = finalSchedule.getTeam();
		return new FinalScheduleDto.FinalScheduleResponse(
				finalSchedule.getId(),
				team.getId(),
				team.getPerformance().getId(),
				team.getName(),
				team.getConfirmedSong(),
				finalSchedule.getStartDateTime(),
				finalSchedule.getEndDateTime(),
				finalSchedule.getMemo()
		);
	}
}
