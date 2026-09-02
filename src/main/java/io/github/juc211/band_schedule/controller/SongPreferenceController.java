package io.github.juc211.band_schedule.controller;

import io.github.juc211.band_schedule.dto.SongPreferenceDto;
import io.github.juc211.band_schedule.service.AdminAuthService;
import io.github.juc211.band_schedule.service.SongPreferenceService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
public class SongPreferenceController {

	private final SongPreferenceService songPreferenceService;
	private final AdminAuthService adminAuthService;

	/**
	 * 링크 기반 확정곡 선호도 제출 및 전체 교체
	 */
	@PutMapping("/song-preferences/{token}")
	public ResponseEntity<List<SongPreferenceDto.SongPreferenceResponse>> submitSongPreferences(
			@PathVariable String token,
			@Valid @RequestBody SongPreferenceDto.SongPreferenceSubmitRequest request
	) {
		return ResponseEntity.ok(songPreferenceService.submitSongPreferences(token, request));
	}

	/**
	 * 공연 참여 인원별 확정곡 선호도 조회
	 */
	@GetMapping("/performances/{performanceId}/performance-members/{performanceMemberId}/song-preferences")
	public ResponseEntity<List<SongPreferenceDto.SongPreferenceResponse>> getSongPreferencesByPerformanceMember(
			@PathVariable Long performanceId,
			@PathVariable Long performanceMemberId
	) {
		return ResponseEntity.ok(songPreferenceService.getSongPreferencesByPerformanceMember(performanceId, performanceMemberId));
	}

	/**
	 * 공연 확정곡 선호도 결과 조회
	 */
	@GetMapping("/performances/{performanceId}/song-preferences/results")
	public ResponseEntity<List<SongPreferenceDto.SongPreferenceResultResponse>> getSongPreferenceResults(@PathVariable Long performanceId) {
		return ResponseEntity.ok(songPreferenceService.getSongPreferenceResults(performanceId));
	}

	/**
	 * 선호도 응답 삭제
	 */
	@DeleteMapping("/song-preferences/{songPreferenceId}")
	public ResponseEntity<Void> deleteSongPreference(
			@RequestHeader(value = "X-Club-Admin-Token", required = false) String clubAdminToken,
			@PathVariable Long songPreferenceId
	) {
		adminAuthService.requireClubAdminForSongPreference(clubAdminToken, songPreferenceId);
		songPreferenceService.deleteSongPreference(songPreferenceId);
		return ResponseEntity.noContent().build();
	}
}
