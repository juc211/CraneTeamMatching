package io.github.juc211.band_schedule.dto;

import io.github.juc211.band_schedule.domain.Part;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public abstract class TeamDto {

	public record TeamCreateRequest(
			@NotBlank(message = "팀 이름은 필수입니다.")
			@Size(max = 100, message = "팀 이름은 100자 이하여야 합니다.")
			String name,

			@Size(max = 200, message = "팀 확정곡은 200자 이하여야 합니다.")
			String confirmedSong,

			@Positive(message = "공연 확정곡 ID는 양수여야 합니다.")
			Long performanceConfirmedSongId
	) {
	}

	public record TeamUpdateRequest(
			@NotBlank(message = "팀 이름은 필수입니다.")
			@Size(max = 100, message = "팀 이름은 100자 이하여야 합니다.")
			String name,

			@Size(max = 200, message = "팀 확정곡은 200자 이하여야 합니다.")
			String confirmedSong,

			@Positive(message = "공연 확정곡 ID는 양수여야 합니다.")
			Long performanceConfirmedSongId
	) {
	}

	public record TeamConfirmedSongUpdateRequest(
			@Size(max = 200, message = "팀 확정곡은 200자 이하여야 합니다.")
			String confirmedSong,

			@Positive(message = "공연 확정곡 ID는 양수여야 합니다.")
			Long performanceConfirmedSongId
	) {
	}

	public record TeamConfirmedSongResponse(
			Long teamId,
			String confirmedSong
	) {
	}

	public record TeamResponse(
			Long teamId,
			Long performanceId,
			String name,
			String confirmedSong
	) {
	}

	public record TeamMemberAddRequest(
			@NotNull(message = "공연 참여 인원 ID는 필수입니다.")
			@Positive(message = "공연 참여 인원 ID는 양수여야 합니다.")
			Long performanceMemberId,

			@NotNull(message = "파트는 필수입니다.")
			Part part
	) {
	}

	public record TeamMemberUpdateRequest(
			@NotNull(message = "파트는 필수입니다.")
			Part part
	) {
	}

	public record TeamMemberResponse(
			Long teamMemberId,
			Long teamId,
			Long performanceMemberId,
			Long userId,
			String name,
			Part part
	) {
	}
}
