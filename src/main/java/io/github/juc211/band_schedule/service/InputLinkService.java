package io.github.juc211.band_schedule.service;

import io.github.juc211.band_schedule.domain.InputLink;
import io.github.juc211.band_schedule.domain.InputLinkType;
import io.github.juc211.band_schedule.domain.Performance;
import io.github.juc211.band_schedule.domain.PerformanceMember;
import io.github.juc211.band_schedule.domain.TeamMember;
import io.github.juc211.band_schedule.dto.InputLinkDto;
import io.github.juc211.band_schedule.exception.BusinessException;
import io.github.juc211.band_schedule.exception.ErrorCode;
import io.github.juc211.band_schedule.repository.InputLinkRepository;
import io.github.juc211.band_schedule.repository.PerformanceMemberRepository;
import io.github.juc211.band_schedule.repository.PerformanceRepository;
import io.github.juc211.band_schedule.repository.TeamMemberRepository;
import java.time.LocalDateTime;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class InputLinkService {

	private static final int TOKEN_BYTE_LENGTH = 24;

	private final InputLinkRepository inputLinkRepository;
	private final PerformanceRepository performanceRepository;
	private final PerformanceMemberRepository performanceMemberRepository;
	private final TeamMemberRepository teamMemberRepository;
	private final SecureRandom secureRandom = new SecureRandom();

	/**
	 * 공연 입력/조회 링크 생성
	 */
	public InputLinkDto.InputLinkResponse createInputLink(Long performanceId, InputLinkDto.InputLinkCreateRequest request) {
		Performance performance = performanceRepository.findById(performanceId)
				.orElseThrow(() -> new BusinessException(ErrorCode.PERFORMANCE_NOT_FOUND, "Performance not found: " + performanceId));

		if (request.type() == null) {
			throw new BusinessException(ErrorCode.INPUT_LINK_TYPE_REQUIRED, "InputLink type is required");
		}

		InputLink savedInputLink = inputLinkRepository.save(InputLink.create(
				generateUniqueToken(),
				performance,
				request.type(),
				true,
				request.expiresAt()
		));

		return toInputLinkResponse(savedInputLink);
	}

	/**
	 * 공연 링크 목록 조회
	 */
	@Transactional(readOnly = true)
	public List<InputLinkDto.InputLinkResponse> getInputLinksByPerformance(Long performanceId) {
		validatePerformanceExists(performanceId);

		return inputLinkRepository.findByPerformanceIdOrderByIdAsc(performanceId)
				.stream()
				.map(this::toInputLinkResponse)
				.toList();
	}

	/**
	 * 링크 활성 상태 수정
	 */
	public InputLinkDto.InputLinkResponse updateInputLinkActive(Long inputLinkId, InputLinkDto.InputLinkActiveUpdateRequest request) {
		InputLink inputLink = inputLinkRepository.findById(inputLinkId)
				.orElseThrow(() -> new BusinessException(ErrorCode.INPUT_LINK_NOT_FOUND, "InputLink not found: " + inputLinkId));

		inputLink.updateActive(request.active());

		return toInputLinkResponse(inputLink);
	}

	/**
	 * 링크 마감일 수정
	 */
	public InputLinkDto.InputLinkResponse updateInputLinkExpiresAt(Long inputLinkId, InputLinkDto.InputLinkExpiresAtUpdateRequest request) {
		InputLink inputLink = inputLinkRepository.findById(inputLinkId)
				.orElseThrow(() -> new BusinessException(ErrorCode.INPUT_LINK_NOT_FOUND, "InputLink not found: " + inputLinkId));

		inputLink.updateExpiresAt(request.expiresAt());

		return toInputLinkResponse(inputLink);
	}

	/**
	 * 링크 삭제
	 */
	public void deleteInputLink(Long inputLinkId) {
		InputLink inputLink = inputLinkRepository.findById(inputLinkId)
				.orElseThrow(() -> new BusinessException(ErrorCode.INPUT_LINK_NOT_FOUND, "InputLink not found: " + inputLinkId));

		inputLinkRepository.delete(inputLink);
	}

	/**
	 * 링크 기반 이름/학번 공연 참여 인원 식별
	 */
	@Transactional(readOnly = true)
	public InputLinkDto.InputLinkIdentifyResponse identifyPerformanceMember(String token, InputLinkDto.InputLinkIdentifyRequest request) {
		InputLink inputLink = inputLinkRepository.findByToken(token)
				.orElseThrow(() -> new BusinessException(ErrorCode.INPUT_LINK_NOT_FOUND, "InputLink not found: " + token));

		validateUsableLink(inputLink);
		validateInputLinkType(inputLink);

		PerformanceMember performanceMember = performanceMemberRepository
				.findByPerformanceIdAndUserNameAndUserStudentNumber(
						inputLink.getPerformance().getId(),
						request.name(),
						request.studentNumber()
				)
				.orElseThrow(() -> new BusinessException(ErrorCode.PERFORMANCE_MEMBER_NOT_FOUND, "PerformanceMember not found by name and student number"));

		return toInputLinkIdentifyResponse(inputLink, performanceMember);
	}

	/**
	 * 고유 토큰 생성
	 */
	private String generateUniqueToken() {
		String token;
		do {
			byte[] bytes = new byte[TOKEN_BYTE_LENGTH];
			secureRandom.nextBytes(bytes);
			token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
		} while (inputLinkRepository.existsByToken(token));
		return token;
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
	 * 링크 사용 가능 여부 검증
	 */
	private void validateUsableLink(InputLink inputLink) {
		if (!inputLink.isActive()) {
			throw new BusinessException(ErrorCode.INPUT_LINK_INACTIVE, "InputLink is inactive");
		}
		if (inputLink.getExpiresAt() != null && inputLink.getExpiresAt().isBefore(LocalDateTime.now())) {
			throw new BusinessException(ErrorCode.LINK_EXPIRED, "InputLink is expired");
		}
	}

	/**
	 * 사용자 입력 링크 타입 여부 검증
	 */
	private void validateInputLinkType(InputLink inputLink) {
		if (inputLink.getType() != InputLinkType.SONG_REQUEST
				&& inputLink.getType() != InputLinkType.SONG_VOTE
				&& inputLink.getType() != InputLinkType.SONG_PREFERENCE
				&& inputLink.getType() != InputLinkType.AVAILABLE_TIME) {
			throw new BusinessException(ErrorCode.INPUT_LINK_NOT_FOR_MEMBER_INPUT, "InputLink type is not for member input");
		}
	}

	/**
	 * 입력 링크 응답 변환
	 */
	private InputLinkDto.InputLinkResponse toInputLinkResponse(InputLink inputLink) {
		return new InputLinkDto.InputLinkResponse(
				inputLink.getId(),
				inputLink.getPerformance().getId(),
				inputLink.getType(),
				inputLink.getToken(),
				inputLink.isActive(),
				inputLink.getExpiresAt(),
				inputLink.getCreatedAt()
		);
	}

	/**
	 * 링크 기반 공연 참여 인원 식별 응답 변환
	 */
	private InputLinkDto.InputLinkIdentifyResponse toInputLinkIdentifyResponse(InputLink inputLink, PerformanceMember performanceMember) {
		return new InputLinkDto.InputLinkIdentifyResponse(
				inputLink.getPerformance().getId(),
				performanceMember.getId(),
				performanceMember.getUser().getId(),
				performanceMember.getUser().getName(),
				performanceMember.getUser().getStudentNumber(),
				teamMemberRepository.findByPerformanceMemberIdOrderByIdAsc(performanceMember.getId())
						.stream()
						.map(this::toInputLinkIdentifyTeamMemberResponse)
						.toList()
		);
	}

	/**
	 * 링크 기반 팀원 식별 응답 변환
	 */
	private InputLinkDto.InputLinkIdentifyTeamMemberResponse toInputLinkIdentifyTeamMemberResponse(TeamMember teamMember) {
		return new InputLinkDto.InputLinkIdentifyTeamMemberResponse(
				teamMember.getId(),
				teamMember.getTeam().getId(),
				teamMember.getTeam().getName(),
				teamMember.getPart()
		);
	}
}
