package io.github.juc211.band_schedule.dto;

import io.github.juc211.band_schedule.domain.Vote;
import java.time.LocalDateTime;

public abstract class SongDto {

	public record SongRequestCreateRequest(
			Long teamId,
			Long requestedByMemberId,
			String song,
			String youtubeUrl
	) {
		public SongRequestCreateRequest(Long teamId, Long requestedByMemberId, String song) {
			this(teamId, requestedByMemberId, song, null);
		}
	}

	public record SongRequestUpdateRequest(
			Long teamId,
			String song,
			String youtubeUrl
	) {
		public SongRequestUpdateRequest(Long teamId, String song) {
			this(teamId, song, null);
		}
	}

	public record SongRequestResponse(
			Long songRequestId,
			Long performanceId,
			Long teamId,
			Long requestedByMemberId,
			String song,
			String youtubeUrl,
			LocalDateTime createdAt
	) {
	}

	public record SongVoteSubmitRequest(
			Long songRequestId,
			Long voterMemberId,
			Vote vote,
			String reason
	) {
	}

	public record SongVoteResponse(
			Long songVoteId,
			Long songRequestId,
			Long voterMemberId,
			Vote vote,
			String reason
	) {
	}
}
