package io.github.juc211.band_schedule.dto;

import io.github.juc211.band_schedule.domain.Vote;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public abstract class SongDto {

	public record SongRequestCreateRequest(
			@Positive(message = "팀 ID는 양수여야 합니다.")
			Long teamId,

			@NotNull(message = "신청자 공연 참여 인원 ID는 필수입니다.")
			@Positive(message = "신청자 공연 참여 인원 ID는 양수여야 합니다.")
			Long requestedByMemberId,

			@NotBlank(message = "희망곡은 필수입니다.")
			@Size(max = 200, message = "희망곡은 200자 이하여야 합니다.")
			String song,

			@Size(max = 500, message = "유튜브 URL은 500자 이하여야 합니다.")
			String youtubeUrl
	) {
		public SongRequestCreateRequest(Long teamId, Long requestedByMemberId, String song) {
			this(teamId, requestedByMemberId, song, null);
		}
	}

	public record SongRequestUpdateRequest(
			@Positive(message = "팀 ID는 양수여야 합니다.")
			Long teamId,

			@NotBlank(message = "희망곡은 필수입니다.")
			@Size(max = 200, message = "희망곡은 200자 이하여야 합니다.")
			String song,

			@Size(max = 500, message = "유튜브 URL은 500자 이하여야 합니다.")
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
			@NotNull(message = "희망곡 신청 ID는 필수입니다.")
			@Positive(message = "희망곡 신청 ID는 양수여야 합니다.")
			Long songRequestId,

			@NotNull(message = "투표자 공연 참여 인원 ID는 필수입니다.")
			@Positive(message = "투표자 공연 참여 인원 ID는 양수여야 합니다.")
			Long voterMemberId,

			@NotNull(message = "투표 값은 필수입니다.")
			Vote vote,

			@Size(max = 500, message = "투표 사유는 500자 이하여야 합니다.")
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
