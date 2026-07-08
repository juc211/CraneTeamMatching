package io.github.juc211.band_schedule.controller;

import io.github.juc211.band_schedule.dto.InputLinkDto;
import io.github.juc211.band_schedule.dto.PerformanceDto;
import io.github.juc211.band_schedule.service.InputLinkService;
import io.github.juc211.band_schedule.service.PerformanceService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class InputLinkController {

	private final InputLinkService inputLinkService;
	private final PerformanceService performanceService;

	/**
	 * 공연 입력/조회 링크 생성
	 */
	@PostMapping("/performances/{performanceId}/input-links")
	public ResponseEntity<InputLinkDto.InputLinkResponse> createInputLink(
			@PathVariable Long performanceId,
			@RequestBody InputLinkDto.InputLinkCreateRequest request
	) {
		InputLinkDto.InputLinkResponse response = inputLinkService.createInputLink(performanceId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	/**
	 * 공연 링크 목록 조회
	 */
	@GetMapping("/performances/{performanceId}/input-links")
	public ResponseEntity<List<InputLinkDto.InputLinkResponse>> getInputLinksByPerformance(@PathVariable Long performanceId) {
		return ResponseEntity.ok(inputLinkService.getInputLinksByPerformance(performanceId));
	}

	/**
	 * 링크 활성 상태 수정
	 */
	@PatchMapping("/input-links/{inputLinkId}/active")
	public ResponseEntity<InputLinkDto.InputLinkResponse> updateInputLinkActive(
			@PathVariable Long inputLinkId,
			@RequestBody InputLinkDto.InputLinkActiveUpdateRequest request
	) {
		return ResponseEntity.ok(inputLinkService.updateInputLinkActive(inputLinkId, request));
	}

	/**
	 * 링크 마감일 수정(null이면 무기한)
	 */
	@PatchMapping("/input-links/{inputLinkId}/expires-at")
	public ResponseEntity<InputLinkDto.InputLinkResponse> updateInputLinkExpiresAt(
			@PathVariable Long inputLinkId,
			@RequestBody InputLinkDto.InputLinkExpiresAtUpdateRequest request
	) {
		return ResponseEntity.ok(inputLinkService.updateInputLinkExpiresAt(inputLinkId, request));
	}

	/**
	 * 링크 삭제
	 */
	@DeleteMapping("/input-links/{inputLinkId}")
	public ResponseEntity<Void> deleteInputLink(@PathVariable Long inputLinkId) {
		inputLinkService.deleteInputLink(inputLinkId);
		return ResponseEntity.noContent().build();
	}

	/**
	 * 링크 기반 공연 정보 조회
	 */
	@GetMapping("/input-links/{token}/performance")
	public ResponseEntity<PerformanceDto.PerformanceResponse> getPerformanceByLink(@PathVariable String token) {
		return ResponseEntity.ok(performanceService.getPerformanceByLink(token));
	}

	/**
	 * 링크 기반 공연 합주 기간 조회
	 */
	@GetMapping("/input-links/{token}/schedule-window")
	public ResponseEntity<PerformanceDto.PerformanceScheduleWindowResponse> getPerformanceScheduleWindowByLink(@PathVariable String token) {
		return ResponseEntity.ok(performanceService.getPerformanceScheduleWindowByLink(token));
	}

	/**
	 * 링크 기반 공연 참여 인원 목록 조회
	 */
	@GetMapping("/input-links/{token}/performance-members")
	public ResponseEntity<List<PerformanceDto.PerformanceMemberResponse>> getPerformanceMembersByLink(@PathVariable String token) {
		return ResponseEntity.ok(performanceService.getPerformanceMembersByLink(token));
	}
}
