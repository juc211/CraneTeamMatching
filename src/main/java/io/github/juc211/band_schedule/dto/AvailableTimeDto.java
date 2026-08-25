package io.github.juc211.band_schedule.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

public abstract class AvailableTimeDto {

	public record AvailableTimeRequest(
			@NotNull(message = "가능 시간 시작은 필수입니다.")
			LocalDateTime startDateTime,

			@NotNull(message = "가능 시간 종료는 필수입니다.")
			LocalDateTime endDateTime
	) {
	}

	public record AvailableTimesReplaceRequest(
			@NotNull(message = "가능 시간 목록은 필수입니다.")
			List<@Valid @NotNull(message = "가능 시간 항목은 필수입니다.") AvailableTimeRequest> availableTimes
	) {
	}

	public record AvailableTimeResponse(
			Long availableTimeId,
			Long teamMemberId,
			Long teamId,
			Long performanceMemberId,
			Long userId,
			String name,
			LocalDateTime startDateTime,
			LocalDateTime endDateTime
	) {
	}

	public record AvailableTimeOverlapResponse(
			Long teamId,
			int requiredTeamMemberCount,
			int availableTeamMemberCount,
			LocalDateTime startDateTime,
			LocalDateTime endDateTime
	) {
	}
}
