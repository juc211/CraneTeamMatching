package io.github.juc211.band_schedule.service;

import io.github.juc211.band_schedule.domain.InputLink;
import io.github.juc211.band_schedule.domain.Performance;
import io.github.juc211.band_schedule.dto.InputLinkDto;
import io.github.juc211.band_schedule.repository.InputLinkRepository;
import io.github.juc211.band_schedule.repository.PerformanceRepository;
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
	private final SecureRandom secureRandom = new SecureRandom();

	/**
	 * 공연 입력/조회 링크 생성
	 */
	public InputLinkDto.InputLinkResponse createInputLink(Long performanceId, InputLinkDto.InputLinkCreateRequest request) {
		Performance performance = performanceRepository.findById(performanceId)
				.orElseThrow(() -> new IllegalArgumentException("Performance not found: " + performanceId));

		if (request.type() == null) {
			throw new IllegalArgumentException("InputLink type is required");
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
				.orElseThrow(() -> new IllegalArgumentException("InputLink not found: " + inputLinkId));

		inputLink.updateActive(request.active());

		return toInputLinkResponse(inputLink);
	}

	/**
	 * 링크 마감일 수정
	 */
	public InputLinkDto.InputLinkResponse updateInputLinkExpiresAt(Long inputLinkId, InputLinkDto.InputLinkExpiresAtUpdateRequest request) {
		InputLink inputLink = inputLinkRepository.findById(inputLinkId)
				.orElseThrow(() -> new IllegalArgumentException("InputLink not found: " + inputLinkId));

		inputLink.updateExpiresAt(request.expiresAt());

		return toInputLinkResponse(inputLink);
	}

	/**
	 * 링크 삭제
	 */
	public void deleteInputLink(Long inputLinkId) {
		InputLink inputLink = inputLinkRepository.findById(inputLinkId)
				.orElseThrow(() -> new IllegalArgumentException("InputLink not found: " + inputLinkId));

		inputLinkRepository.delete(inputLink);
	}

	private String generateUniqueToken() {
		String token;
		do {
			byte[] bytes = new byte[TOKEN_BYTE_LENGTH];
			secureRandom.nextBytes(bytes);
			token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
		} while (inputLinkRepository.existsByToken(token));
		return token;
	}

	private void validatePerformanceExists(Long performanceId) {
		if (!performanceRepository.existsById(performanceId)) {
			throw new IllegalArgumentException("Performance not found: " + performanceId);
		}
	}

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
}
