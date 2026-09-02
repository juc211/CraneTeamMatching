package io.github.juc211.band_schedule.controller;

import io.github.juc211.band_schedule.domain.UserStatus;
import io.github.juc211.band_schedule.dto.UserDto;
import io.github.juc211.band_schedule.service.AdminAuthService;
import io.github.juc211.band_schedule.service.UserService;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

	private final UserService userService;
	private final AdminAuthService adminAuthService;

	/**
	 * 밴드 멤버 생성
	 */
	@PostMapping
	public ResponseEntity<UserDto.UserCreateResponse> createUser(
			@RequestHeader(value = "X-Master-Admin-Token", required = false) String masterAdminToken,
			@Valid @RequestBody UserDto.UserCreateRequest request
	) {
		adminAuthService.requireMasterAdmin(masterAdminToken);
		UserDto.UserCreateResponse response = userService.createUser(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	/**
	 * 유저 조회(status가 있으면 상태별 조회)
	 */
	@GetMapping
	public ResponseEntity<List<UserDto.UserResponse>> getUsers(
			@RequestHeader(value = "X-Master-Admin-Token", required = false) String masterAdminToken,
			@RequestParam(required = false) UserStatus status
	) {
		adminAuthService.requireMasterAdmin(masterAdminToken);
		return ResponseEntity.ok(userService.getUsersByStatus(status));
	}

	/**
	 * 유저 단건 조회
	 */
	@GetMapping("/{userId}")
	public ResponseEntity<UserDto.UserResponse> getUser(
			@RequestHeader(value = "X-Master-Admin-Token", required = false) String masterAdminToken,
			@PathVariable Long userId
	) {
		adminAuthService.requireMasterAdmin(masterAdminToken);
		return ResponseEntity.ok(userService.getUser(userId));
	}

	/**
	 * 유저 수정
	 */
	@PatchMapping("/{userId}")
	public ResponseEntity<UserDto.UserResponse> updateUser(
			@RequestHeader(value = "X-Master-Admin-Token", required = false) String masterAdminToken,
			@PathVariable Long userId,
			@Valid @RequestBody UserDto.UserUpdateRequest request
	) {
		adminAuthService.requireMasterAdmin(masterAdminToken);
		return ResponseEntity.ok(userService.updateUser(userId, request));
	}

	/**
	 * 유저 상태 수정
	 */
	@PatchMapping("/{userId}/status")
	public ResponseEntity<UserDto.UserResponse> updateUserStatus(
			@RequestHeader(value = "X-Master-Admin-Token", required = false) String masterAdminToken,
			@PathVariable Long userId,
			@Valid @RequestBody UserDto.UserStatusUpdateRequest request
	) {
		adminAuthService.requireMasterAdmin(masterAdminToken);
		return ResponseEntity.ok(userService.updateUserStatus(userId, request));
	}

	/**
	 * 잘못 생성된 유저 삭제(참조가 없을 때만 가능)
	 */
	@DeleteMapping("/{userId}")
	public ResponseEntity<Void> deleteUser(
			@RequestHeader(value = "X-Master-Admin-Token", required = false) String masterAdminToken,
			@PathVariable Long userId
	) {
		adminAuthService.requireMasterAdmin(masterAdminToken);
		userService.deleteUser(userId);
		return ResponseEntity.noContent().build();
	}
}
