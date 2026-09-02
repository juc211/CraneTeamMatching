package io.github.juc211.band_schedule.controller;

import io.github.juc211.band_schedule.dto.FinalScheduleDto;
import io.github.juc211.band_schedule.service.AdminAuthService;
import io.github.juc211.band_schedule.service.FinalScheduleService;
import java.util.List;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class FinalScheduleController {

	private final FinalScheduleService finalScheduleService;
	private final AdminAuthService adminAuthService;

	/**
	 * 팀 최종 합주 일정 생성
	 */
	@PostMapping("/teams/{teamId}/final-schedules")
	public ResponseEntity<FinalScheduleDto.FinalScheduleResponse> createFinalSchedule(
			@RequestHeader(value = "X-Club-Admin-Token", required = false) String clubAdminToken,
			@PathVariable Long teamId,
			@Valid @RequestBody FinalScheduleDto.FinalScheduleCreateRequest request
	) {
		adminAuthService.requireClubAdminForTeam(clubAdminToken, teamId);
		FinalScheduleDto.FinalScheduleResponse response = finalScheduleService.createFinalSchedule(teamId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	/**
	 * 팀 최종 합주 일정 목록 조회
	 */
	@GetMapping("/teams/{teamId}/final-schedules")
	public ResponseEntity<List<FinalScheduleDto.FinalScheduleResponse>> getFinalSchedulesByTeam(@PathVariable Long teamId) {
		return ResponseEntity.ok(finalScheduleService.getFinalSchedulesByTeam(teamId));
	}

	/**
	 * 공연 전체 최종 합주 일정 목록 조회
	 */
	@GetMapping("/performances/{performanceId}/final-schedules")
	public ResponseEntity<List<FinalScheduleDto.FinalScheduleResponse>> getFinalSchedulesByPerformance(@PathVariable Long performanceId) {
		return ResponseEntity.ok(finalScheduleService.getFinalSchedulesByPerformance(performanceId));
	}

	/**
	 * 링크 기반 공연 전체 최종 합주 일정 조회
	 */
	@GetMapping("/input-links/{token}/final-schedules")
	public ResponseEntity<List<FinalScheduleDto.FinalScheduleResponse>> getFinalSchedulesByLink(@PathVariable String token) {
		return ResponseEntity.ok(finalScheduleService.getFinalSchedulesByLink(token));
	}

	/**
	 * 링크 기반 특정 팀 최종 합주 일정 조회
	 */
	@GetMapping("/input-links/{token}/teams/{teamId}/final-schedules")
	public ResponseEntity<List<FinalScheduleDto.FinalScheduleResponse>> getFinalSchedulesByLinkAndTeam(
			@PathVariable String token,
			@PathVariable Long teamId
	) {
		return ResponseEntity.ok(finalScheduleService.getFinalSchedulesByLinkAndTeam(token, teamId));
	}

	/**
	 * 최종 합주 일정 수정
	 */
	@PatchMapping("/final-schedules/{finalScheduleId}")
	public ResponseEntity<FinalScheduleDto.FinalScheduleResponse> updateFinalSchedule(
			@RequestHeader(value = "X-Club-Admin-Token", required = false) String clubAdminToken,
			@PathVariable Long finalScheduleId,
			@Valid @RequestBody FinalScheduleDto.FinalScheduleUpdateRequest request
	) {
		adminAuthService.requireClubAdminForFinalSchedule(clubAdminToken, finalScheduleId);
		return ResponseEntity.ok(finalScheduleService.updateFinalSchedule(finalScheduleId, request));
	}

	/**
	 * 최종 합주 일정 삭제
	 */
	@DeleteMapping("/final-schedules/{finalScheduleId}")
	public ResponseEntity<Void> deleteFinalSchedule(
			@RequestHeader(value = "X-Club-Admin-Token", required = false) String clubAdminToken,
			@PathVariable Long finalScheduleId
	) {
		adminAuthService.requireClubAdminForFinalSchedule(clubAdminToken, finalScheduleId);
		finalScheduleService.deleteFinalSchedule(finalScheduleId);
		return ResponseEntity.noContent().build();
	}
}
