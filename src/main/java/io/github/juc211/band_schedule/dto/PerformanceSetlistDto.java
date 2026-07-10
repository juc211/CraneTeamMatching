package io.github.juc211.band_schedule.dto;

import java.util.List;

public abstract class PerformanceSetlistDto {

	public record PerformanceSetlistReplaceRequest(
			List<PerformanceSetlistItemRequest> items
	) {
	}

	public record PerformanceSetlistItemRequest(
			Long teamId,
			Integer sequenceNumber
	) {
	}

	public record PerformanceSetlistItemResponse(
			Long setlistItemId,
			Long performanceId,
			Long teamId,
			String teamName,
			String confirmedSong,
			Integer sequenceNumber
	) {
	}
}
