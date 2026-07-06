package io.github.juc211.band_schedule.controller;

import io.github.juc211.band_schedule.dto.TeamDto;
import io.github.juc211.band_schedule.service.TeamService;
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
	 * 팀 단위 확정곡 조회
	 */
	@GetMapping("/teams/{teamId}/confirmed-song")
	public ResponseEntity<TeamDto.TeamConfirmedSongResponse> getTeamConfirmedSong(@PathVariable Long teamId) {
		return ResponseEntity.ok(teamService.getTeamConfirmedSong(teamId));
	}

	/**
	 * 팀 단위 확정곡 지정/수정
	 */
	@PatchMapping("/teams/{teamId}/confirmed-song")
	public ResponseEntity<TeamDto.TeamResponse> updateTeamConfirmedSong(
			@PathVariable Long teamId,
			@RequestBody TeamDto.TeamConfirmedSongUpdateRequest request
	) {
		return ResponseEntity.ok(teamService.updateTeamConfirmedSong(teamId, request));
	}

	/**
	 * 팀 단위 확정곡 삭제
	 */
	@DeleteMapping("/teams/{teamId}/confirmed-song")
	public ResponseEntity<Void> deleteTeamConfirmedSong(@PathVariable Long teamId) {
		teamService.deleteTeamConfirmedSong(teamId);
		return ResponseEntity.noContent().build();
	}

	/**
	 * 공연별 팀 목록 조회
	 */
	@GetMapping("/performances/{performanceId}/teams")
	public ResponseEntity<List<TeamDto.TeamResponse>> getTeamsByPerformance(@PathVariable Long performanceId) {
		return ResponseEntity.ok(teamService.getTeamsByPerformance(performanceId));
	}

	/**
	 * 링크 기반 공연 팀 목록 조회
	 */
	@GetMapping("/input-links/{token}/teams")
	public ResponseEntity<List<TeamDto.TeamResponse>> getTeamsByLink(@PathVariable String token) {
		return ResponseEntity.ok(teamService.getTeamsByLink(token));
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
	 * 팀원 파트 수정
	 */
	@PatchMapping("/team-members/{teamMemberId}")
	public ResponseEntity<TeamDto.TeamMemberResponse> updateTeamMember(
			@PathVariable Long teamMemberId,
			@RequestBody TeamDto.TeamMemberUpdateRequest request
	) {
		return ResponseEntity.ok(teamService.updateTeamMember(teamMemberId, request));
	}

	/**
	 * 팀 내부 팀원 목록 조회
	 */
	@GetMapping("/teams/{teamId}/members")
	public ResponseEntity<List<TeamDto.TeamMemberResponse>> getTeamMembers(@PathVariable Long teamId) {
		return ResponseEntity.ok(teamService.getTeamMembers(teamId));
	}

	/**
	 * 링크 기반 팀 내부 팀원 목록 조회
	 */
	@GetMapping("/input-links/{token}/teams/{teamId}/members")
	public ResponseEntity<List<TeamDto.TeamMemberResponse>> getTeamMembersByLink(
			@PathVariable String token,
			@PathVariable Long teamId
	) {
		return ResponseEntity.ok(teamService.getTeamMembersByLink(token, teamId));
	}

	/**
	 * 팀원 삭제(가능 시간도 함께 삭제)
	 */
	@DeleteMapping("/team-members/{teamMemberId}")
	public ResponseEntity<Void> deleteTeamMember(@PathVariable Long teamMemberId) {
		teamService.deleteTeamMember(teamMemberId);
		return ResponseEntity.noContent().build();
	}

	/**
	 * 팀 삭제(팀 하위 데이터도 함께 삭제)
	 */
	@DeleteMapping("/teams/{teamId}")
	public ResponseEntity<Void> deleteTeam(@PathVariable Long teamId) {
		teamService.deleteTeam(teamId);
		return ResponseEntity.noContent().build();
	}
}
