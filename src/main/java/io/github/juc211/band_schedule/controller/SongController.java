package io.github.juc211.band_schedule.controller;

import io.github.juc211.band_schedule.dto.SongDto;
import io.github.juc211.band_schedule.service.SongService;
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
public class SongController {

	private final SongService songService;

	/**
	 * 희망곡 신청
	 */
	@PostMapping("/song-requests/{token}")
	public ResponseEntity<SongDto.SongRequestResponse> createSongRequest(
			@PathVariable String token,
			@RequestBody SongDto.SongRequestCreateRequest request
	) {
		SongDto.SongRequestResponse response = songService.createSongRequest(token, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	/**
	 * 공연 전체 희망곡 조회
	 */
	@GetMapping("/performances/{performanceId}/song-requests")
	public ResponseEntity<List<SongDto.SongRequestResponse>> getSongRequestsByPerformance(@PathVariable Long performanceId) {
		return ResponseEntity.ok(songService.getSongRequestsByPerformance(performanceId));
	}

	/**
	 * 링크 기반 공연 전체 희망곡 조회
	 */
	@GetMapping("/input-links/{token}/song-requests")
	public ResponseEntity<List<SongDto.SongRequestResponse>> getSongRequestsByLink(@PathVariable String token) {
		return ResponseEntity.ok(songService.getSongRequestsByLink(token));
	}

	/**
	 * 팀 단위 희망곡 조회
	 */
	@GetMapping("/teams/{teamId}/song-requests")
	public ResponseEntity<List<SongDto.SongRequestResponse>> getSongRequestsByTeam(@PathVariable Long teamId) {
		return ResponseEntity.ok(songService.getSongRequestsByTeam(teamId));
	}

	/**
	 * 링크 기반 팀 단위 희망곡 조회
	 */
	@GetMapping("/input-links/{token}/teams/{teamId}/song-requests")
	public ResponseEntity<List<SongDto.SongRequestResponse>> getSongRequestsByLinkAndTeam(
			@PathVariable String token,
			@PathVariable Long teamId
	) {
		return ResponseEntity.ok(songService.getSongRequestsByLinkAndTeam(token, teamId));
	}

	/**
	 * 희망곡 수정
	 */
	@PatchMapping("/song-requests/{songRequestId}")
	public ResponseEntity<SongDto.SongRequestResponse> updateSongRequest(
			@PathVariable Long songRequestId,
			@RequestBody SongDto.SongRequestUpdateRequest request
	) {
		return ResponseEntity.ok(songService.updateSongRequest(songRequestId, request));
	}

	/**
	 * 희망곡 삭제
	 */
	@DeleteMapping("/song-requests/{songRequestId}")
	public ResponseEntity<Void> deleteSongRequest(@PathVariable Long songRequestId) {
		songService.deleteSongRequest(songRequestId);
		return ResponseEntity.noContent().build();
	}

	/**
	 * 희망곡 투표 제출
	 */
	@PostMapping("/song-vote/{token}")
	public ResponseEntity<SongDto.SongVoteResponse> submitSongVote(
			@PathVariable String token,
			@RequestBody SongDto.SongVoteSubmitRequest request
	) {
		SongDto.SongVoteResponse response = songService.submitSongVote(token, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	/**
	 * 특정 희망곡의 투표 목록 조회
	 */
	@GetMapping("/song-requests/{songRequestId}/votes")
	public ResponseEntity<List<SongDto.SongVoteResponse>> getSongVotesBySongRequest(@PathVariable Long songRequestId) {
		return ResponseEntity.ok(songService.getSongVotesBySongRequest(songRequestId));
	}

	/**
	 * 희망곡 투표 삭제
	 */
	@DeleteMapping("/song-votes/{songVoteId}")
	public ResponseEntity<Void> deleteSongVote(@PathVariable Long songVoteId) {
		songService.deleteSongVote(songVoteId);
		return ResponseEntity.noContent().build();
	}

}
