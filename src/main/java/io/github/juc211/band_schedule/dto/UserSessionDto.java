package io.github.juc211.band_schedule.dto;

import io.github.juc211.band_schedule.domain.Part;
import jakarta.validation.constraints.NotNull;

public abstract class UserSessionDto {

	public record UserSessionCreateRequest(
			@NotNull(message = "파트는 필수입니다.")
			Part part
	) {
	}

	public record UserSessionResponse(
			Long userSessionId,
			Long userId,
			Part part
	) {
	}
}
