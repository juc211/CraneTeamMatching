package io.github.juc211.band_schedule.dto;

import java.util.List;

public abstract class SongPreferenceDto {

	public record SongPreferenceSubmitRequest(
			Long performanceMemberId,
			List<SongPreferenceItemRequest> preferences
	) {
	}

	public record SongPreferenceItemRequest(
			Long performanceConfirmedSongId,
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
