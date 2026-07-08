package io.github.juc211.band_schedule.service;

import io.github.juc211.band_schedule.domain.AvailableTime;
import io.github.juc211.band_schedule.domain.InputLink;
import io.github.juc211.band_schedule.domain.InputLinkType;
import io.github.juc211.band_schedule.domain.Performance;
import io.github.juc211.band_schedule.domain.TeamMember;
import io.github.juc211.band_schedule.dto.AvailableTimeDto;
import io.github.juc211.band_schedule.repository.AvailabilityRepository;
import io.github.juc211.band_schedule.repository.InputLinkRepository;
import io.github.juc211.band_schedule.repository.TeamRepository;
import io.github.juc211.band_schedule.repository.TeamMemberRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AvailableTimeService {

	private final AvailabilityRepository availabilityRepository;
	private final TeamMemberRepository teamMemberRepository;
	private final TeamRepository teamRepository;
	private final InputLinkRepository inputLinkRepository;

	/**
	 * 팀원 가능 시간 목록 전체 저장/교체
	 */
	public List<AvailableTimeDto.AvailableTimeResponse> replaceAvailableTimesByTeamMember(
			Long teamMemberId,
			AvailableTimeDto.AvailableTimesReplaceRequest request
	) {
		TeamMember teamMember = teamMemberRepository.findById(teamMemberId)
				.orElseThrow(() -> new IllegalArgumentException("TeamMember not found: " + teamMemberId));

		return replaceAvailableTimes(teamMember, request);
	}

	/**
	 * 링크 기반 팀원 가능 시간 목록 전체 저장/교체
	 */
	public List<AvailableTimeDto.AvailableTimeResponse> replaceAvailableTimesByTeamMember(
			String token,
			Long teamMemberId,
			AvailableTimeDto.AvailableTimesReplaceRequest request
	) {
		InputLink inputLink = inputLinkRepository.findByToken(token)
				.orElseThrow(() -> new IllegalArgumentException("InputLink not found: " + token));
		TeamMember teamMember = teamMemberRepository.findById(teamMemberId)
				.orElseThrow(() -> new IllegalArgumentException("TeamMember not found: " + teamMemberId));

		validateUsableLink(inputLink);
		validateLinkType(inputLink, InputLinkType.AVAILABLE_TIME);
		validateSamePerformance(inputLink, teamMember);

		return replaceAvailableTimes(teamMember, request);
	}

	private List<AvailableTimeDto.AvailableTimeResponse> replaceAvailableTimes(
			TeamMember teamMember,
			AvailableTimeDto.AvailableTimesReplaceRequest request
	) {
		List<AvailableTimeDto.AvailableTimeRequest> availableTimeRequests = request.availableTimes() == null
				? List.of()
				: request.availableTimes();

		availableTimeRequests.forEach(availableTimeRequest ->
				validateWithinScheduleWindow(teamMember, availableTimeRequest.startDateTime(), availableTimeRequest.endDateTime())
		);

		availabilityRepository.deleteByTeamMemberId(teamMember.getId());

		return availabilityRepository.saveAll(
						availableTimeRequests.stream()
								.map(availableTimeRequest -> AvailableTime.create(
										teamMember,
										availableTimeRequest.startDateTime(),
										availableTimeRequest.endDateTime()
								))
								.toList()
				)
				.stream()
				.map(this::toAvailableTimeResponse)
				.toList();
	}

	/**
	 * 팀원별 가능 시간 목록 조회
	 */
	@Transactional(readOnly = true)
	public List<AvailableTimeDto.AvailableTimeResponse> getAvailableTimesByTeamMember(Long teamMemberId) {
		validateTeamMemberExists(teamMemberId);

		return availabilityRepository.findByTeamMemberIdOrderByStartDateTimeAscIdAsc(teamMemberId)
				.stream()
				.map(this::toAvailableTimeResponse)
				.toList();
	}

	/**
	 * 링크 기반 팀원별 가능 시간 목록 조회
	 */
	@Transactional(readOnly = true)
	public List<AvailableTimeDto.AvailableTimeResponse> getAvailableTimesByTeamMember(String token, Long teamMemberId) {
		InputLink inputLink = inputLinkRepository.findByToken(token)
				.orElseThrow(() -> new IllegalArgumentException("InputLink not found: " + token));
		TeamMember teamMember = teamMemberRepository.findById(teamMemberId)
				.orElseThrow(() -> new IllegalArgumentException("TeamMember not found: " + teamMemberId));

		validateUsableLink(inputLink);
		validateLinkType(inputLink, InputLinkType.AVAILABLE_TIME);
		validateSamePerformance(inputLink, teamMember);

		return availabilityRepository.findByTeamMemberIdOrderByStartDateTimeAscIdAsc(teamMemberId)
				.stream()
				.map(this::toAvailableTimeResponse)
				.toList();
	}

	/**
	 * 팀 전체 가능 시간 목록 조회
	 */
	@Transactional(readOnly = true)
	public List<AvailableTimeDto.AvailableTimeResponse> getAvailableTimesByTeam(Long teamId) {
		validateTeamExists(teamId);

		return availabilityRepository.findByTeamMemberTeamIdOrderByStartDateTimeAscIdAsc(teamId)
				.stream()
				.map(this::toAvailableTimeResponse)
				.toList();
	}

	/**
	 * 팀 전체 공통 가능 시간 조회
	 */
	@Transactional(readOnly = true)
	public List<AvailableTimeDto.AvailableTimeOverlapResponse> getAvailableTimeOverlapsByTeam(Long teamId) {
		validateTeamExists(teamId);

		List<TeamMember> teamMembers = teamMemberRepository.findByTeamIdOrderByIdAsc(teamId);
		if (teamMembers.isEmpty()) {
			return List.of();
		}

		List<TimeRange> overlaps = null;
		for (TeamMember teamMember : teamMembers) {
			List<TimeRange> memberRanges = getNormalizedAvailableTimeRanges(teamMember.getId());
			if (memberRanges.isEmpty()) {
				return List.of();
			}
			if (overlaps == null) {
				overlaps = memberRanges;
			} else {
				overlaps = intersect(overlaps, memberRanges);
			}
			if (overlaps.isEmpty()) {
				return List.of();
			}
		}

		int teamMemberCount = teamMembers.size();
		return overlaps.stream()
				.map(timeRange -> new AvailableTimeDto.AvailableTimeOverlapResponse(
						teamId,
						teamMemberCount,
						teamMemberCount,
						timeRange.startDateTime(),
						timeRange.endDateTime()
				))
				.toList();
	}

	private void validateTeamMemberExists(Long teamMemberId) {
		if (!teamMemberRepository.existsById(teamMemberId)) {
			throw new IllegalArgumentException("TeamMember not found: " + teamMemberId);
		}
	}

	private void validateTeamExists(Long teamId) {
		if (!teamRepository.existsById(teamId)) {
			throw new IllegalArgumentException("Team not found: " + teamId);
		}
	}

	private void validateWithinScheduleWindow(TeamMember teamMember, LocalDateTime startDateTime, LocalDateTime endDateTime) {
		Performance performance = teamMember.getTeam().getPerformance();
		LocalDate scheduleWindowStartDate = performance.getScheduleWindowStartDate();
		LocalDate scheduleWindowEndDate = performance.getScheduleWindowEndDate();

		if (scheduleWindowStartDate == null || scheduleWindowEndDate == null) {
			throw new IllegalArgumentException("Performance schedule window is required to submit available time");
		}
		if (startDateTime == null || endDateTime == null) {
			throw new IllegalArgumentException("Available time start and end date time must be set together");
		}
		if (startDateTime.toLocalDate().isBefore(scheduleWindowStartDate)
				|| endDateTime.toLocalDate().isAfter(scheduleWindowEndDate)) {
			throw new IllegalArgumentException("Available time must be within performance schedule window");
		}
	}

	private void validateUsableLink(InputLink inputLink) {
		if (!inputLink.isActive()) {
			throw new IllegalArgumentException("InputLink is inactive");
		}
		if (inputLink.getExpiresAt() != null && inputLink.getExpiresAt().isBefore(LocalDateTime.now())) {
			throw new IllegalArgumentException("InputLink is expired");
		}
	}

	private void validateLinkType(InputLink inputLink, InputLinkType expectedType) {
		if (inputLink.getType() != expectedType) {
			throw new IllegalArgumentException("InputLink type must be " + expectedType);
		}
	}

	private void validateSamePerformance(InputLink inputLink, TeamMember teamMember) {
		Long linkPerformanceId = inputLink.getPerformance().getId();
		Long memberPerformanceId = teamMember.getTeam().getPerformance().getId();
		if (!linkPerformanceId.equals(memberPerformanceId)) {
			throw new IllegalArgumentException("TeamMember does not belong to link performance");
		}
	}

	private List<TimeRange> getNormalizedAvailableTimeRanges(Long teamMemberId) {
		List<TimeRange> sortedRanges = availabilityRepository.findByTeamMemberIdOrderByStartDateTimeAscIdAsc(teamMemberId)
				.stream()
				.map(availableTime -> new TimeRange(availableTime.getStartDateTime(), availableTime.getEndDateTime()))
				.sorted(Comparator.comparing(TimeRange::startDateTime).thenComparing(TimeRange::endDateTime))
				.toList();

		List<TimeRange> normalizedRanges = new ArrayList<>();
		for (TimeRange currentRange : sortedRanges) {
			if (normalizedRanges.isEmpty()) {
				normalizedRanges.add(currentRange);
				continue;
			}

			TimeRange lastRange = normalizedRanges.get(normalizedRanges.size() - 1);
			if (!currentRange.startDateTime().isAfter(lastRange.endDateTime())) {
				normalizedRanges.set(
						normalizedRanges.size() - 1,
						new TimeRange(lastRange.startDateTime(), max(lastRange.endDateTime(), currentRange.endDateTime()))
				);
			} else {
				normalizedRanges.add(currentRange);
			}
		}

		return normalizedRanges;
	}

	private List<TimeRange> intersect(List<TimeRange> leftRanges, List<TimeRange> rightRanges) {
		List<TimeRange> intersections = new ArrayList<>();
		int leftIndex = 0;
		int rightIndex = 0;

		while (leftIndex < leftRanges.size() && rightIndex < rightRanges.size()) {
			TimeRange leftRange = leftRanges.get(leftIndex);
			TimeRange rightRange = rightRanges.get(rightIndex);
			LocalDateTime startDateTime = max(leftRange.startDateTime(), rightRange.startDateTime());
			LocalDateTime endDateTime = min(leftRange.endDateTime(), rightRange.endDateTime());

			if (startDateTime.isBefore(endDateTime)) {
				intersections.add(new TimeRange(startDateTime, endDateTime));
			}

			if (leftRange.endDateTime().isBefore(rightRange.endDateTime())) {
				leftIndex++;
			} else {
				rightIndex++;
			}
		}

		return intersections;
	}

	private LocalDateTime max(LocalDateTime first, LocalDateTime second) {
		return first.isAfter(second) ? first : second;
	}

	private LocalDateTime min(LocalDateTime first, LocalDateTime second) {
		return first.isBefore(second) ? first : second;
	}

	private AvailableTimeDto.AvailableTimeResponse toAvailableTimeResponse(AvailableTime availableTime) {
		TeamMember teamMember = availableTime.getTeamMember();
		return new AvailableTimeDto.AvailableTimeResponse(
				availableTime.getId(),
				teamMember.getId(),
				teamMember.getTeam().getId(),
				teamMember.getPerformanceMember().getId(),
				teamMember.getPerformanceMember().getUser().getId(),
				teamMember.getPerformanceMember().getUser().getName(),
				availableTime.getStartDateTime(),
				availableTime.getEndDateTime()
		);
	}

	private record TimeRange(
			LocalDateTime startDateTime,
			LocalDateTime endDateTime
	) {
	}
}
