package io.github.juc211.band_schedule.service;

import io.github.juc211.band_schedule.domain.Performance;
import io.github.juc211.band_schedule.domain.PerformanceSetlistItem;
import io.github.juc211.band_schedule.domain.Team;
import io.github.juc211.band_schedule.dto.PerformanceSetlistDto;
import io.github.juc211.band_schedule.exception.BusinessException;
import io.github.juc211.band_schedule.exception.ErrorCode;
import io.github.juc211.band_schedule.repository.PerformanceRepository;
import io.github.juc211.band_schedule.repository.PerformanceSetlistItemRepository;
import io.github.juc211.band_schedule.repository.TeamRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PerformanceSetlistService {

	private final PerformanceRepository performanceRepository;
	private final PerformanceSetlistItemRepository performanceSetlistItemRepository;
	private final TeamRepository teamRepository;

	/**
	 * 공연 셋리스트 전체 지정 및 교체
	 */
	public List<PerformanceSetlistDto.PerformanceSetlistItemResponse> replaceSetlist(
			Long performanceId,
			PerformanceSetlistDto.PerformanceSetlistReplaceRequest request
	) {
		Performance performance = performanceRepository.findById(performanceId)
				.orElseThrow(() -> new BusinessException(ErrorCode.PERFORMANCE_NOT_FOUND, "Performance not found: " + performanceId));

		List<PerformanceSetlistDto.PerformanceSetlistItemRequest> items = request.items() == null
				? List.of()
				: request.items();
		List<PerformanceSetlistItem> setlistItems = validateAndCreateSetlistItems(performance, items);

		performanceSetlistItemRepository.deleteByPerformanceId(performanceId);
		performanceSetlistItemRepository.saveAll(setlistItems);

		return performanceSetlistItemRepository.findByPerformanceIdOrderBySequenceNumberAscIdAsc(performanceId)
				.stream()
				.map(this::toSetlistItemResponse)
				.toList();
	}

	/**
	 * 공연 셋리스트 조회
	 */
	@Transactional(readOnly = true)
	public List<PerformanceSetlistDto.PerformanceSetlistItemResponse> getSetlist(Long performanceId) {
		validatePerformanceExists(performanceId);

		return performanceSetlistItemRepository.findByPerformanceIdOrderBySequenceNumberAscIdAsc(performanceId)
				.stream()
				.map(this::toSetlistItemResponse)
				.toList();
	}

	/**
	 * 공연 셋리스트 전체 삭제
	 */
	public void deleteSetlist(Long performanceId) {
		validatePerformanceExists(performanceId);

		performanceSetlistItemRepository.deleteByPerformanceId(performanceId);
	}

	/**
	 * 셋리스트 항목 검증 후 엔티티 생성
	 */
	private List<PerformanceSetlistItem> validateAndCreateSetlistItems(
			Performance performance,
			List<PerformanceSetlistDto.PerformanceSetlistItemRequest> items
	) {
		Set<Long> teamIds = new HashSet<>();
		Set<Integer> sequenceNumbers = new HashSet<>();

		return items.stream()
				.map(item -> {
					validateSetlistItemRequest(item, teamIds, sequenceNumbers);
					Team team = teamRepository.findById(item.teamId())
							.orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_FOUND, "Team not found: " + item.teamId()));
					validateTeamBelongsToPerformance(performance, team);
					return PerformanceSetlistItem.create(performance, team, item.sequenceNumber());
				})
				.toList();
	}

	/**
	 * 셋리스트 요청 값 검증
	 */
	private void validateSetlistItemRequest(
			PerformanceSetlistDto.PerformanceSetlistItemRequest item,
			Set<Long> teamIds,
			Set<Integer> sequenceNumbers
	) {
		if (item == null) {
			throw new BusinessException(ErrorCode.SETLIST_ITEM_REQUIRED, "Setlist item is required");
		}
		if (item.teamId() == null) {
			throw new BusinessException(ErrorCode.SETLIST_TEAM_ID_REQUIRED, "Setlist team id is required");
		}
		if (item.sequenceNumber() == null || item.sequenceNumber() < 1) {
			throw new BusinessException(ErrorCode.SETLIST_SEQUENCE_INVALID, "Setlist sequence number must be positive");
		}
		if (!teamIds.add(item.teamId())) {
			throw new BusinessException(ErrorCode.DUPLICATE_SETLIST_TEAM, "Setlist cannot contain duplicate team");
		}
		if (!sequenceNumbers.add(item.sequenceNumber())) {
			throw new BusinessException(ErrorCode.DUPLICATE_SETLIST_SEQUENCE, "Setlist cannot contain duplicate sequence number");
		}
	}

	/**
	 * 팀이 공연에 속하는지 검증
	 */
	private void validateTeamBelongsToPerformance(Performance performance, Team team) {
		if (!team.getPerformance().getId().equals(performance.getId())) {
			throw new BusinessException(ErrorCode.TEAM_NOT_IN_PERFORMANCE, "Team does not belong to performance");
		}
	}

	/**
	 * 공연 존재 여부 검증
	 */
	private void validatePerformanceExists(Long performanceId) {
		if (!performanceRepository.existsById(performanceId)) {
			throw new BusinessException(ErrorCode.PERFORMANCE_NOT_FOUND, "Performance not found: " + performanceId);
		}
	}

	/**
	 * 셋리스트 응답 변환
	 */
	private PerformanceSetlistDto.PerformanceSetlistItemResponse toSetlistItemResponse(PerformanceSetlistItem setlistItem) {
		Team team = setlistItem.getTeam();
		return new PerformanceSetlistDto.PerformanceSetlistItemResponse(
				setlistItem.getId(),
				setlistItem.getPerformance().getId(),
				team.getId(),
				team.getName(),
				team.getConfirmedSong(),
				setlistItem.getSequenceNumber()
		);
	}
}
