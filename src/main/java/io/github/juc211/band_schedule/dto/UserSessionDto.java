package io.github.juc211.band_schedule.dto;

import io.github.juc211.band_schedule.domain.Part;

public abstract class UserSessionDto {

	public record UserSessionCreateRequest(
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
