package io.github.juc211.band_schedule.dto;

import java.time.LocalDateTime;

public abstract class FinalScheduleDto {

	public record FinalScheduleCreateRequest(
			LocalDateTime startDateTime,
			LocalDateTime endDateTime,
			String memo
	) {
	}

	public record FinalScheduleUpdateRequest(
			LocalDateTime startDateTime,
			LocalDateTime endDateTime,
			String memo
	) {
	}

	public record FinalScheduleResponse(
			Long finalScheduleId,
			Long teamId,
			Long performanceId,
			String teamName,
			String confirmedSong,
			LocalDateTime startDateTime,
			LocalDateTime endDateTime,
			String memo
	) {
	}
}
