package io.github.juc211.band_schedule.dto;

import java.time.LocalDateTime;

public abstract class PerformanceConfirmedSongDto {

	public record PerformanceConfirmedSongCreateRequest(
			String song
	) {
	}

	public record PerformanceConfirmedSongUpdateRequest(
			String song
	) {
	}

	public record PerformanceConfirmedSongResponse(
			Long performanceConfirmedSongId,
			Long performanceId,
			String song,
			LocalDateTime createdAt
	) {
	}
}
