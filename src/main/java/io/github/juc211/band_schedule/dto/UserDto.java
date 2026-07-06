package io.github.juc211.band_schedule.dto;

import io.github.juc211.band_schedule.domain.UserStatus;

public abstract class UserDto {

	public record UserCreateRequest(
			String name,
			String studentNumber
	) {
	}

	public record UserCreateResponse(
			Long userId,
			String name,
			String studentNumber,
			UserStatus status
	) {
	}

	public record UserUpdateRequest(
			String name,
			String studentNumber
	) {
	}

	public record UserStatusUpdateRequest(
			UserStatus status
	) {
	}

	public record UserResponse(
			Long userId,
			String name,
			String studentNumber,
			UserStatus status
	) {
	}
}
