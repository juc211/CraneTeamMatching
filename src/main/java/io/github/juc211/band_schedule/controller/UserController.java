package io.github.juc211.band_schedule.controller;

import io.github.juc211.band_schedule.dto.UserDto;
import io.github.juc211.band_schedule.service.UserService;
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
@RequestMapping("/api/users")
public class UserController {

	private final UserService userService;

	/**
	 * 밴드 멤버 생성
	 */
	@PostMapping
	public ResponseEntity<UserDto.UserCreateResponse> createUser(@RequestBody UserDto.UserCreateRequest request) {
		UserDto.UserCreateResponse response = userService.createUser(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	/**
	 * 유저 조회
	 */
	@GetMapping
	public ResponseEntity<List<UserDto.UserResponse>> getUsers() {
		return ResponseEntity.ok(userService.getUsers());
	}

	/**
	 * 유저 단건 조회
	 */
	@GetMapping("/{userId}")
	public ResponseEntity<UserDto.UserResponse> getUser(@PathVariable Long userId) {
		return ResponseEntity.ok(userService.getUser(userId));
	}

	/**
	 * 유저 수정
	 */
	@PatchMapping("/{userId}")
	public ResponseEntity<UserDto.UserResponse> updateUser(
			@PathVariable Long userId,
			@RequestBody UserDto.UserUpdateRequest request
	) {
		return ResponseEntity.ok(userService.updateUser(userId, request));
	}
}
