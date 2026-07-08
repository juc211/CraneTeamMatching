package io.github.juc211.band_schedule.dto;

import io.github.juc211.band_schedule.domain.InputLinkType;
import java.time.LocalDateTime;

public abstract class InputLinkDto {

	public record InputLinkCreateRequest(
			InputLinkType type,
			LocalDateTime expiresAt
	) {
	}

	public record InputLinkActiveUpdateRequest(
			boolean active
	) {
	}

	public record InputLinkExpiresAtUpdateRequest(
			LocalDateTime expiresAt
	) {
	}

	public record InputLinkResponse(
			Long inputLinkId,
			Long performanceId,
			InputLinkType type,
			String token,
			boolean active,
			LocalDateTime expiresAt,
			LocalDateTime createdAt
	) {
	}
}
