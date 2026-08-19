package io.github.juc211.band_schedule.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public abstract class FinalScheduleDto {

	public record FinalScheduleCreateRequest(
			@NotNull(message = "합주 시작 시간은 필수입니다.")
			LocalDateTime startDateTime,

			@NotNull(message = "합주 종료 시간은 필수입니다.")
			LocalDateTime endDateTime,
			String memo
	) {
	}

	public record FinalScheduleUpdateRequest(
			@NotNull(message = "합주 시작 시간은 필수입니다.")
			LocalDateTime startDateTime,

			@NotNull(message = "합주 종료 시간은 필수입니다.")
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
