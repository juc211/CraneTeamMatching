package io.github.juc211.band_schedule.dto;

import io.github.juc211.band_schedule.domain.Part;

public abstract class TeamDto {

	public record TeamCreateRequest(
			String name,
			String confirmedSong,
			Long performanceConfirmedSongId
	) {
	}

	public record TeamUpdateRequest(
			String name,
			String confirmedSong,
			Long performanceConfirmedSongId
	) {
	}

	public record TeamConfirmedSongUpdateRequest(
			String confirmedSong,
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
			Long performanceMemberId,
			Part part
	) {
	}

	public record TeamMemberUpdateRequest(
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
