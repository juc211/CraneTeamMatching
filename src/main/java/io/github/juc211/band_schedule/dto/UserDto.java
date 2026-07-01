package io.github.juc211.band_schedule.dto;

public abstract class UserDto {

	public record UserCreateRequest(
			String name,
			String studentNumber
	) {
	}

	public record UserCreateResponse(
			Long userId,
			String name,
			String studentNumber
	) {
	}

	public record UserUpdateRequest(
			String name,
			String studentNumber
	) {
	}

	public record UserResponse(
			Long userId,
			String name,
			String studentNumber
	) {
	}
}
