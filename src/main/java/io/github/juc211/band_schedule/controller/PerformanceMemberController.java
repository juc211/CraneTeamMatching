package io.github.juc211.band_schedule.controller;

import io.github.juc211.band_schedule.service.AdminAuthService;
import io.github.juc211.band_schedule.service.PerformanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/performance-members")
public class PerformanceMemberController {

	private final PerformanceService performanceService;
	private final AdminAuthService adminAuthService;

	/**
	 * 공연 참여 인원 삭제(연결된 팀원/가능 시간/신청곡/투표도 함께 삭제)
	 */
	@DeleteMapping("/{performanceMemberId}")
	public ResponseEntity<Void> deletePerformanceMember(
			@RequestHeader(value = "X-Club-Admin-Token", required = false) String clubAdminToken,
			@PathVariable Long performanceMemberId
	) {
		adminAuthService.requireClubAdminForPerformanceMember(clubAdminToken, performanceMemberId);
		performanceService.deletePerformanceMember(performanceMemberId);
		return ResponseEntity.noContent().build();
	}
}
