package io.github.juc211.band_schedule.dto;

import io.github.juc211.band_schedule.domain.InputLinkType;
import io.github.juc211.band_schedule.domain.Part;
import java.time.LocalDateTime;
import java.util.List;

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

	public record InputLinkIdentifyRequest(
			String name,
			String studentNumber
	) {
	}

	public record InputLinkIdentifyResponse(
			Long performanceId,
			Long performanceMemberId,
			Long userId,
			String name,
			String studentNumber,
			List<InputLinkIdentifyTeamMemberResponse> teamMembers
	) {
	}

	public record InputLinkIdentifyTeamMemberResponse(
			Long teamMemberId,
			Long teamId,
			String teamName,
			Part part
	) {
	}
}
