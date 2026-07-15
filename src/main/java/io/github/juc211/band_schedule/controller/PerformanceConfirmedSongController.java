package io.github.juc211.band_schedule.controller;

import io.github.juc211.band_schedule.dto.PerformanceConfirmedSongDto;
import io.github.juc211.band_schedule.service.PerformanceConfirmedSongService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
/**
 * 확정곡이 팀단위가 아닌 공연 단위로 결정 될 때(크레인 - 동아리박람회, src버스킹, 벚꽃축제 등등)
 */
public class PerformanceConfirmedSongController {

	private final PerformanceConfirmedSongService performanceConfirmedSongService;

	/**
	 * 공연 단위 확정곡 추가
	 */
	@PostMapping("/performances/{performanceId}/performance-confirmed-songs")
	public ResponseEntity<PerformanceConfirmedSongDto.PerformanceConfirmedSongResponse> createPerformanceConfirmedSong(
			@PathVariable Long performanceId,
			@RequestBody PerformanceConfirmedSongDto.PerformanceConfirmedSongCreateRequest request
	) {
		PerformanceConfirmedSongDto.PerformanceConfirmedSongResponse response = performanceConfirmedSongService.createPerformanceConfirmedSong(performanceId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	/**
	 * 공연 단위 확정곡 목록 조회
	 */
	@GetMapping("/performances/{performanceId}/performance-confirmed-songs")
	public ResponseEntity<List<PerformanceConfirmedSongDto.PerformanceConfirmedSongResponse>> getPerformanceConfirmedSongsByPerformance(@PathVariable Long performanceId) {
		return ResponseEntity.ok(performanceConfirmedSongService.getPerformanceConfirmedSongsByPerformance(performanceId));
	}

	/**
	 * 링크 기반 공연 단위 확정곡 목록 조회
	 */
	@GetMapping("/input-links/{token}/performance-confirmed-songs")
	public ResponseEntity<List<PerformanceConfirmedSongDto.PerformanceConfirmedSongPublicResponse>> getPerformanceConfirmedSongsByLink(@PathVariable String token) {
		return ResponseEntity.ok(performanceConfirmedSongService.getPerformanceConfirmedSongsByLink(token));
	}

	/**
	 * 공연 단위 확정곡 수정
	 */
	@PatchMapping("/performance-confirmed-songs/{performanceConfirmedSongId}")
	public ResponseEntity<PerformanceConfirmedSongDto.PerformanceConfirmedSongResponse> updatePerformanceConfirmedSong(
			@PathVariable Long performanceConfirmedSongId,
			@RequestBody PerformanceConfirmedSongDto.PerformanceConfirmedSongUpdateRequest request
	) {
		return ResponseEntity.ok(performanceConfirmedSongService.updatePerformanceConfirmedSong(performanceConfirmedSongId, request));
	}

	/**
	 * 공연 단위 확정곡 삭제
	 */
	@DeleteMapping("/performance-confirmed-songs/{performanceConfirmedSongId}")
	public ResponseEntity<Void> deletePerformanceConfirmedSong(@PathVariable Long performanceConfirmedSongId) {
		performanceConfirmedSongService.deletePerformanceConfirmedSong(performanceConfirmedSongId);
		return ResponseEntity.noContent().build();
	}
}
