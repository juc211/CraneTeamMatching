package io.github.juc211.band_schedule.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public abstract class PerformanceConfirmedSongDto {

	public record PerformanceConfirmedSongCreateRequest(
			@NotBlank(message = "공연 확정곡은 필수입니다.")
			@Size(max = 200, message = "공연 확정곡은 200자 이하여야 합니다.")
			String song,

			@Size(max = 1000, message = "관리자 메모는 1000자 이하여야 합니다.")
			String adminMemo
	) {
		public PerformanceConfirmedSongCreateRequest(String song) {
			this(song, null);
		}
	}

	public record PerformanceConfirmedSongUpdateRequest(
			@NotBlank(message = "공연 확정곡은 필수입니다.")
			@Size(max = 200, message = "공연 확정곡은 200자 이하여야 합니다.")
			String song,

			@Size(max = 1000, message = "관리자 메모는 1000자 이하여야 합니다.")
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
