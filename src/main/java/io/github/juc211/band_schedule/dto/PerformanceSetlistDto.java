package io.github.juc211.band_schedule.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

public abstract class PerformanceSetlistDto {

	public record PerformanceSetlistReplaceRequest(
			@NotNull(message = "셋리스트 항목 목록은 필수입니다.")
			List<@Valid @NotNull(message = "셋리스트 항목은 필수입니다.") PerformanceSetlistItemRequest> items
	) {
	}

	public record PerformanceSetlistItemRequest(
			@NotNull(message = "셋리스트 팀 ID는 필수입니다.")
			@Positive(message = "셋리스트 팀 ID는 양수여야 합니다.")
			Long teamId,

			@NotNull(message = "셋리스트 순서는 필수입니다.")
			@Positive(message = "셋리스트 순서는 1 이상이어야 합니다.")
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
