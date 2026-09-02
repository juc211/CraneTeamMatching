package io.github.juc211.band_schedule.controller;

import io.github.juc211.band_schedule.dto.PerformanceSetlistDto;
import io.github.juc211.band_schedule.service.AdminAuthService;
import io.github.juc211.band_schedule.service.PerformanceSetlistService;
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
public class PerformanceSetlistController {

	private final PerformanceSetlistService performanceSetlistService;
	private final AdminAuthService adminAuthService;

	/**
	 * 공연 셋리스트 전체 지정 및 교체
	 */
	@PutMapping("/performances/{performanceId}/setlist")
	public ResponseEntity<List<PerformanceSetlistDto.PerformanceSetlistItemResponse>> replaceSetlist(
			@RequestHeader(value = "X-Club-Admin-Token", required = false) String clubAdminToken,
			@PathVariable Long performanceId,
			@Valid @RequestBody PerformanceSetlistDto.PerformanceSetlistReplaceRequest request
	) {
		adminAuthService.requireClubAdminForPerformance(clubAdminToken, performanceId);
		return ResponseEntity.ok(performanceSetlistService.replaceSetlist(performanceId, request));
	}

	/**
	 * 공연 셋리스트 조회
	 */
	@GetMapping("/performances/{performanceId}/setlist")
	public ResponseEntity<List<PerformanceSetlistDto.PerformanceSetlistItemResponse>> getSetlist(@PathVariable Long performanceId) {
		return ResponseEntity.ok(performanceSetlistService.getSetlist(performanceId));
	}

	/**
	 * 공연 셋리스트 전체 삭제
	 */
	@DeleteMapping("/performances/{performanceId}/setlist")
	public ResponseEntity<Void> deleteSetlist(
			@RequestHeader(value = "X-Club-Admin-Token", required = false) String clubAdminToken,
			@PathVariable Long performanceId
	) {
		adminAuthService.requireClubAdminForPerformance(clubAdminToken, performanceId);
		performanceSetlistService.deleteSetlist(performanceId);
		return ResponseEntity.noContent().build();
	}
}
