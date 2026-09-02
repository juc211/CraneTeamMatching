package io.github.juc211.band_schedule.dto;

import io.github.juc211.band_schedule.domain.UserStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public abstract class UserDto {

	public record UserCreateRequest(
			@NotBlank(message = "이름은 필수입니다.")
			@Size(max = 50, message = "이름은 50자 이하여야 합니다.")
			String name,

			@NotBlank(message = "학번은 필수입니다.")
			@Size(max = 8, message = "학번은 8자리 숫자여야 합니다.")
			@Pattern(regexp = "\\d{8}", message = "학번은 8자리 숫자여야 합니다.")
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
			@NotBlank(message = "이름은 필수입니다.")
			@Size(max = 50, message = "이름은 50자 이하여야 합니다.")
			String name,

			@NotBlank(message = "학번은 필수입니다.")
			@Size(max = 8, message = "학번은 8자리 숫자여야 합니다.")
			@Pattern(regexp = "\\d{8}", message = "학번은 8자리 숫자여야 합니다.")
			String studentNumber
	) {
	}

	public record UserStatusUpdateRequest(
			@NotNull(message = "유저 상태는 필수입니다.")
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
