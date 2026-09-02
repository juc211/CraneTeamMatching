package io.github.juc211.band_schedule.controller;

import io.github.juc211.band_schedule.dto.UserSessionDto;
import io.github.juc211.band_schedule.service.AdminAuthService;
import io.github.juc211.band_schedule.service.UserSessionService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserSessionController {

	private final UserSessionService userSessionService;
	private final AdminAuthService adminAuthService;

	/**
	 * 유저 세션 추가
	 */
	@PostMapping("/users/{userId}/sessions")
	public ResponseEntity<UserSessionDto.UserSessionResponse> createUserSession(
			@RequestHeader(value = "X-Master-Admin-Token", required = false) String masterAdminToken,
			@PathVariable Long userId,
			@Valid @RequestBody UserSessionDto.UserSessionCreateRequest request
	) {
		adminAuthService.requireMasterAdmin(masterAdminToken);
		UserSessionDto.UserSessionResponse response = userSessionService.createUserSession(userId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	/**
	 * 유저 세션 목록 조회
	 */
	@GetMapping("/users/{userId}/sessions")
	public ResponseEntity<List<UserSessionDto.UserSessionResponse>> getUserSessionsByUser(@PathVariable Long userId) {
		return ResponseEntity.ok(userSessionService.getUserSessionsByUser(userId));
	}

	/**
	 * 유저 세션 삭제
	 */
	@DeleteMapping("/user-sessions/{userSessionId}")
	public ResponseEntity<Void> deleteUserSession(
			@RequestHeader(value = "X-Master-Admin-Token", required = false) String masterAdminToken,
			@PathVariable Long userSessionId
	) {
		adminAuthService.requireMasterAdmin(masterAdminToken);
		userSessionService.deleteUserSession(userSessionId);
		return ResponseEntity.noContent().build();
	}
}
