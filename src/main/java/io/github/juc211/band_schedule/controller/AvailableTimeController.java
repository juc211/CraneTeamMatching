package io.github.juc211.band_schedule.controller;

import io.github.juc211.band_schedule.dto.AvailableTimeDto;
import io.github.juc211.band_schedule.service.AdminAuthService;
import io.github.juc211.band_schedule.service.AvailableTimeService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AvailableTimeController {

	private final AvailableTimeService availableTimeService;
	private final AdminAuthService adminAuthService;

	/**
	 * 팀원 가능 시간 목록 전체 저장/교체/삭제(빈 배열 넣으면 삭제로 인식) - (관리자용)
	 */
	@PutMapping("/team-members/{teamMemberId}/available-times")
	public ResponseEntity<List<AvailableTimeDto.AvailableTimeResponse>> replaceAvailableTimesByTeamMember(
			@PathVariable Long teamMemberId,
			@RequestHeader(value = "X-Club-Admin-Token", required = false) String clubAdminToken,
			@Valid @RequestBody AvailableTimeDto.AvailableTimesReplaceRequest request
	) {
		adminAuthService.requireClubAdminForAvailableTimeTeamMember(clubAdminToken, teamMemberId);
		return ResponseEntity.ok(availableTimeService.replaceAvailableTimesByTeamMember(teamMemberId, request));
	}

	/**
	 * 링크 기반 팀원 가능 시간 목록 전체 저장/교체 - (유저용)
	 */
	@PutMapping("/input-links/{token}/team-members/{teamMemberId}/available-times")
	public ResponseEntity<List<AvailableTimeDto.AvailableTimeResponse>> replaceAvailableTimesByTeamMember(
			@PathVariable String token,
			@PathVariable Long teamMemberId,
			@Valid @RequestBody AvailableTimeDto.AvailableTimesReplaceRequest request
	) {
		return ResponseEntity.ok(availableTimeService.replaceAvailableTimesByTeamMember(token, teamMemberId, request));
	}

	/**
	 * 팀원별 가능 시간 목록 조회
	 */
	@GetMapping("/team-members/{teamMemberId}/available-times")
	public ResponseEntity<List<AvailableTimeDto.AvailableTimeResponse>> getAvailableTimesByTeamMember(@PathVariable Long teamMemberId) {
		return ResponseEntity.ok(availableTimeService.getAvailableTimesByTeamMember(teamMemberId));
	}

	/**
	 * 링크 기반 팀원별 가능 시간 목록 조회
	 */
	@GetMapping("/input-links/{token}/team-members/{teamMemberId}/available-times")
	public ResponseEntity<List<AvailableTimeDto.AvailableTimeResponse>> getAvailableTimesByTeamMember(
			@PathVariable String token,
			@PathVariable Long teamMemberId
	) {
		return ResponseEntity.ok(availableTimeService.getAvailableTimesByTeamMember(token, teamMemberId));
	}

	/**
	 * 팀 전체 가능 시간 목록 조회
	 */
	@GetMapping("/teams/{teamId}/available-times")
	public ResponseEntity<List<AvailableTimeDto.AvailableTimeResponse>> getAvailableTimesByTeam(@PathVariable Long teamId) {
		return ResponseEntity.ok(availableTimeService.getAvailableTimesByTeam(teamId));
	}

	/**
	 * 팀 전체 공통 가능 시간 조회(교집합)
	 */
	@GetMapping("/teams/{teamId}/available-time-overlaps")
	public ResponseEntity<List<AvailableTimeDto.AvailableTimeOverlapResponse>> getAvailableTimeOverlapsByTeam(@PathVariable Long teamId) {
		return ResponseEntity.ok(availableTimeService.getAvailableTimeOverlapsByTeam(teamId));
	}

}
