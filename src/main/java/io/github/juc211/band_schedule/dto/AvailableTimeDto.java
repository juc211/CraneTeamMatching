package io.github.juc211.band_schedule.dto;

import java.time.LocalDateTime;
import java.util.List;

public abstract class AvailableTimeDto {

	public record AvailableTimeRequest(
			LocalDateTime startDateTime,
			LocalDateTime endDateTime
	) {
	}

	public record AvailableTimesReplaceRequest(
			List<AvailableTimeRequest> availableTimes
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
