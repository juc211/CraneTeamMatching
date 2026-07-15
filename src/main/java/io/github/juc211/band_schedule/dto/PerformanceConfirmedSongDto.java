package io.github.juc211.band_schedule.dto;

import java.time.LocalDateTime;

public abstract class PerformanceConfirmedSongDto {

	public record PerformanceConfirmedSongCreateRequest(
			String song,
			String adminMemo
	) {
		public PerformanceConfirmedSongCreateRequest(String song) {
			this(song, null);
		}
	}

	public record PerformanceConfirmedSongUpdateRequest(
			String song,
			String adminMemo
	) {
		public PerformanceConfirmedSongUpdateRequest(String song) {
			this(song, null);
		}
	}

	public record PerformanceConfirmedSongResponse(
			Long performanceConfirmedSongId,
			Long performanceId,
			String song,
			String adminMemo,
			LocalDateTime createdAt
	) {
	}

	public record PerformanceConfirmedSongPublicResponse(
			Long performanceConfirmedSongId,
			Long performanceId,
			String song,
			LocalDateTime createdAt
	) {
	}
}
