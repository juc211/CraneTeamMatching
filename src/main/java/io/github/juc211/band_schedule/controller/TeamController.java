package io.github.juc211.band_schedule.controller;

import io.github.juc211.band_schedule.dto.TeamDto;
import io.github.juc211.band_schedule.service.TeamService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
public class TeamController {

	private final TeamService teamService;

	/**
	 * 팀 생성
	 */
	@PostMapping("/performances/{performanceId}/teams")
	public ResponseEntity<TeamDto.TeamResponse> createTeam(
			@PathVariable Long performanceId,
			@RequestBody TeamDto.TeamCreateRequest request
	) {
		TeamDto.TeamResponse response = teamService.createTeam(performanceId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	/**
	 * 팀 수정
	 */
	@PatchMapping("/teams/{teamId}")
	public ResponseEntity<TeamDto.TeamResponse> updateTeam(
			@PathVariable Long teamId,
			@RequestBody TeamDto.TeamUpdateRequest request
	) {
		return ResponseEntity.ok(teamService.updateTeam(teamId, request));
	}

	/**
	 * 공연별 팀 목록 조회
	 */
	@GetMapping("/performances/{performanceId}/teams")
	public ResponseEntity<List<TeamDto.TeamResponse>> getTeamsByPerformance(@PathVariable Long performanceId) {
		return ResponseEntity.ok(teamService.getTeamsByPerformance(performanceId));
	}

	/**
	 * 팀원 추가
	 */
	@PostMapping("/teams/{teamId}/members")
	public ResponseEntity<TeamDto.TeamMemberResponse> addTeamMember(
			@PathVariable Long teamId,
			@RequestBody TeamDto.TeamMemberAddRequest request
	) {
		TeamDto.TeamMemberResponse response = teamService.addTeamMember(teamId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	/**
	 * 팀 내부 팀원 목록 조회
	 */
	@GetMapping("/teams/{teamId}/members")
	public ResponseEntity<List<TeamDto.TeamMemberResponse>> getTeamMembers(@PathVariable Long teamId) {
		return ResponseEntity.ok(teamService.getTeamMembers(teamId));
	}
}
