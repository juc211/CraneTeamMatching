package io.github.juc211.band_schedule.dto;

import io.github.juc211.band_schedule.domain.InputLinkType;
import io.github.juc211.band_schedule.domain.Part;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

public abstract class InputLinkDto {

	public record InputLinkCreateRequest(
			@NotNull(message = "링크 타입은 필수입니다.")
			InputLinkType type,
			LocalDateTime expiresAt
	) {
	}

	public record InputLinkActiveUpdateRequest(
			@NotNull(message = "링크 활성 상태는 필수입니다.")
			Boolean active
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
			@NotBlank(message = "이름은 필수입니다.")
			@Size(max = 50, message = "이름은 50자 이하여야 합니다.")
			String name,

			@NotBlank(message = "학번은 필수입니다.")
			@Size(max = 8, message = "학번은 8자리 숫자여야 합니다.")
			@Pattern(regexp = "\\d{8}", message = "학번은 8자리 숫자여야 합니다.")
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
