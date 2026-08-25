package io.github.juc211.band_schedule.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

public abstract class SongPreferenceDto {

	public record SongPreferenceSubmitRequest(
			@NotNull(message = "공연 참여 인원 ID는 필수입니다.")
			@Positive(message = "공연 참여 인원 ID는 양수여야 합니다.")
			Long performanceMemberId,

			@NotNull(message = "선호도 목록은 필수입니다.")
			List<@Valid @NotNull(message = "선호도 항목은 필수입니다.") SongPreferenceItemRequest> preferences
	) {
	}

	public record SongPreferenceItemRequest(
			@NotNull(message = "공연 확정곡 ID는 필수입니다.")
			@Positive(message = "공연 확정곡 ID는 양수여야 합니다.")
			Long performanceConfirmedSongId,

			@NotNull(message = "선호도 순위는 필수입니다.")
			@Positive(message = "선호도 순위는 1 이상이어야 합니다.")
			Integer rank
	) {
	}

	public record SongPreferenceResponse(
			Long songPreferenceId,
			Long performanceConfirmedSongId,
			Long performanceId,
			String song,
			Long performanceMemberId,
			Long userId,
			String userName,
			Integer rank
	) {
	}

	public record SongPreferenceResultResponse(
			Long performanceConfirmedSongId,
			Long performanceId,
			String song,
			String adminMemo,
			long preferenceCount,
			Double averageRank,
			List<SongPreferenceResponse> preferences
	) {
	}
}
