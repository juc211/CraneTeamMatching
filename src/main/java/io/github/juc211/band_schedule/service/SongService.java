package io.github.juc211.band_schedule.service;

import io.github.juc211.band_schedule.domain.InputLink;
import io.github.juc211.band_schedule.domain.InputLinkType;
import io.github.juc211.band_schedule.domain.PerformanceMember;
import io.github.juc211.band_schedule.domain.SongRequest;
import io.github.juc211.band_schedule.domain.SongVote;
import io.github.juc211.band_schedule.domain.Team;
import io.github.juc211.band_schedule.dto.SongDto;
import io.github.juc211.band_schedule.repository.InputLinkRepository;
import io.github.juc211.band_schedule.repository.PerformanceMemberRepository;
import io.github.juc211.band_schedule.repository.PerformanceRepository;
import io.github.juc211.band_schedule.repository.SongRequestRepository;
import io.github.juc211.band_schedule.repository.SongVoteRepository;
import io.github.juc211.band_schedule.repository.TeamRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class SongService {

	private final SongRequestRepository songRequestRepository;
	private final InputLinkRepository inputLinkRepository;
	private final PerformanceMemberRepository performanceMemberRepository;
	private final PerformanceRepository performanceRepository;
	private final TeamRepository teamRepository;
	private final SongVoteRepository songVoteRepository;

	/**
	 * 희망곡 신청
	 */
	public SongDto.SongRequestResponse createSongRequest(String token, SongDto.SongRequestCreateRequest request) {
		InputLink inputLink = inputLinkRepository.findByToken(token)
				.orElseThrow(() -> new IllegalArgumentException("InputLink not found: " + token));
		PerformanceMember requestedByMember = performanceMemberRepository.findById(request.requestedByMemberId())
				.orElseThrow(() -> new IllegalArgumentException("PerformanceMember not found: " + request.requestedByMemberId()));

		validateUsableLink(inputLink);
		validateLinkType(inputLink, InputLinkType.SONG_REQUEST);
		validateSamePerformance(inputLink, requestedByMember);
		Team selectedTeam = getSelectedTeam(request.teamId());
		validateTeamBelongsToLinkPerformance(inputLink, selectedTeam);

		SongRequest savedSongRequest = songRequestRepository.save(SongRequest.create(
				inputLink.getPerformance(),
				selectedTeam,
				requestedByMember,
				request.song()
		));

		return toSongRequestResponse(savedSongRequest);
	}

	/**
	 * 공연 전체 희망곡 조회
	 */
	@Transactional(readOnly = true)
	public List<SongDto.SongRequestResponse> getSongRequestsByPerformance(Long performanceId) {
		validatePerformanceExists(performanceId);

		return songRequestRepository.findByPerformanceIdOrderByIdAsc(performanceId)
				.stream()
				.map(this::toSongRequestResponse)
				.toList();
	}

	/**
	 * 링크 기반 공연 전체 희망곡 조회
	 */
	@Transactional(readOnly = true)
	public List<SongDto.SongRequestResponse> getSongRequestsByLink(String token) {
		InputLink inputLink = inputLinkRepository.findByToken(token)
				.orElseThrow(() -> new IllegalArgumentException("InputLink not found: " + token));

		validateUsableLink(inputLink);
		validateLinkType(inputLink, InputLinkType.SONG_VOTE);

		return songRequestRepository.findByPerformanceIdOrderByIdAsc(inputLink.getPerformance().getId())
				.stream()
				.map(this::toSongRequestResponse)
				.toList();
	}

	/**
	 * 팀 단위 희망곡 조회
	 */
	@Transactional(readOnly = true)
	public List<SongDto.SongRequestResponse> getSongRequestsByTeam(Long teamId) {
		validateTeamExists(teamId);

		return songRequestRepository.findByTeamIdOrderByIdAsc(teamId)
				.stream()
				.map(this::toSongRequestResponse)
				.toList();
	}

	/**
	 * 링크 기반 팀 단위 희망곡 조회
	 */
	@Transactional(readOnly = true)
	public List<SongDto.SongRequestResponse> getSongRequestsByLinkAndTeam(String token, Long teamId) {
		InputLink inputLink = inputLinkRepository.findByToken(token)
				.orElseThrow(() -> new IllegalArgumentException("InputLink not found: " + token));
		Team team = teamRepository.findById(teamId)
				.orElseThrow(() -> new IllegalArgumentException("Team not found: " + teamId));

		validateUsableLink(inputLink);
		validateLinkType(inputLink, InputLinkType.SONG_VOTE);
		validateTeamBelongsToLinkPerformance(inputLink, team);

		return songRequestRepository.findByTeamIdOrderByIdAsc(teamId)
				.stream()
				.map(this::toSongRequestResponse)
				.toList();
	}

	/**
	 * 희망곡 수정
	 */
	public SongDto.SongRequestResponse updateSongRequest(Long songRequestId, SongDto.SongRequestUpdateRequest request) {
		SongRequest songRequest = songRequestRepository.findById(songRequestId)
				.orElseThrow(() -> new IllegalArgumentException("SongRequest not found: " + songRequestId));
		Team selectedTeam = getSelectedTeam(request.teamId());

		validateTeamBelongsToSongRequestPerformance(songRequest, selectedTeam);

		songRequest.update(selectedTeam, request.song());

		return toSongRequestResponse(songRequest);
	}

	/**
	 * 희망곡 삭제
	 */
	public void deleteSongRequest(Long songRequestId) {
		SongRequest songRequest = songRequestRepository.findById(songRequestId)
				.orElseThrow(() -> new IllegalArgumentException("SongRequest not found: " + songRequestId));

		songVoteRepository.deleteBySongRequestId(songRequestId);
		songRequestRepository.delete(songRequest);
	}

	/**
	 * 희망곡 투표 삭제
	 */
	public void deleteSongVote(Long songVoteId) {
		SongVote songVote = songVoteRepository.findById(songVoteId)
				.orElseThrow(() -> new IllegalArgumentException("SongVote not found: " + songVoteId));

		songVoteRepository.delete(songVote);
	}

	/**
	 * 희망곡 투표 제출
	 */
	public SongDto.SongVoteResponse submitSongVote(String token, SongDto.SongVoteSubmitRequest request) {
		InputLink inputLink = inputLinkRepository.findByToken(token)
				.orElseThrow(() -> new IllegalArgumentException("InputLink not found: " + token));
		SongRequest songRequest = songRequestRepository.findById(request.songRequestId())
				.orElseThrow(() -> new IllegalArgumentException("SongRequest not found: " + request.songRequestId()));
		PerformanceMember voterMember = performanceMemberRepository.findById(request.voterMemberId())
				.orElseThrow(() -> new IllegalArgumentException("PerformanceMember not found: " + request.voterMemberId()));

		validateUsableLink(inputLink);
		validateLinkType(inputLink, InputLinkType.SONG_VOTE);
		validateSongRequestBelongsToLinkPerformance(inputLink, songRequest);
		validateSamePerformance(inputLink, voterMember);

		SongVote songVote = songVoteRepository.findBySongRequestIdAndVoterMemberId(songRequest.getId(), voterMember.getId())
				.orElseGet(() -> songVoteRepository.save(SongVote.create(songRequest, voterMember, request.vote(), request.reason())));
		songVote.update(request.vote(), request.reason());

		return toSongVoteResponse(songVote);
	}

	/**
	 * 특정 희망곡의 투표 목록 조회
	 */
	@Transactional(readOnly = true)
	public List<SongDto.SongVoteResponse> getSongVotesBySongRequest(Long songRequestId) {
		validateSongRequestExists(songRequestId);

		return songVoteRepository.findBySongRequestIdOrderByIdAsc(songRequestId)
				.stream()
				.map(this::toSongVoteResponse)
				.toList();
	}

	private void validateUsableLink(InputLink inputLink) {
		if (!inputLink.isActive()) {
			throw new IllegalArgumentException("InputLink is inactive");
		}
		if (inputLink.getExpiresAt() != null && inputLink.getExpiresAt().isBefore(LocalDateTime.now())) {
			throw new IllegalArgumentException("InputLink is expired");
		}
	}

	private void validateLinkType(InputLink inputLink, InputLinkType expectedType) {
		if (inputLink.getType() != expectedType) {
			throw new IllegalArgumentException("InputLink type must be " + expectedType);
		}
	}

	private void validateSamePerformance(InputLink inputLink, PerformanceMember requestedByMember) {
		Long linkPerformanceId = inputLink.getPerformance().getId();
		Long memberPerformanceId = requestedByMember.getPerformance().getId();
		if (!linkPerformanceId.equals(memberPerformanceId)) {
			throw new IllegalArgumentException("PerformanceMember does not belong to link performance");
		}
	}

	private Team getSelectedTeam(Long teamId) {
		if (teamId == null) {
			return null;
		}

		return teamRepository.findById(teamId)
				.orElseThrow(() -> new IllegalArgumentException("Team not found: " + teamId));
	}

	private void validatePerformanceExists(Long performanceId) {
		if (!performanceRepository.existsById(performanceId)) {
			throw new IllegalArgumentException("Performance not found: " + performanceId);
		}
	}

	private void validateTeamExists(Long teamId) {
		if (!teamRepository.existsById(teamId)) {
			throw new IllegalArgumentException("Team not found: " + teamId);
		}
	}

	private void validateSongRequestExists(Long songRequestId) {
		if (!songRequestRepository.existsById(songRequestId)) {
			throw new IllegalArgumentException("SongRequest not found: " + songRequestId);
		}
	}

	private void validateTeamBelongsToLinkPerformance(InputLink inputLink, Team selectedTeam) {
		if (selectedTeam == null) {
			return;
		}

		Long linkPerformanceId = inputLink.getPerformance().getId();
		Long teamPerformanceId = selectedTeam.getPerformance().getId();
		if (!linkPerformanceId.equals(teamPerformanceId)) {
			throw new IllegalArgumentException("Team does not belong to link performance");
		}
	}

	private void validateTeamBelongsToSongRequestPerformance(SongRequest songRequest, Team selectedTeam) {
		if (selectedTeam == null) {
			return;
		}

		Long songRequestPerformanceId = songRequest.getPerformance().getId();
		Long teamPerformanceId = selectedTeam.getPerformance().getId();
		if (!songRequestPerformanceId.equals(teamPerformanceId)) {
			throw new IllegalArgumentException("Team does not belong to song request performance");
		}
	}

	private void validateSongRequestBelongsToLinkPerformance(InputLink inputLink, SongRequest songRequest) {
		Long linkPerformanceId = inputLink.getPerformance().getId();
		Long songRequestPerformanceId = songRequest.getPerformance().getId();
		if (!linkPerformanceId.equals(songRequestPerformanceId)) {
			throw new IllegalArgumentException("SongRequest does not belong to link performance");
		}
	}

	private SongDto.SongRequestResponse toSongRequestResponse(SongRequest songRequest) {
		Team team = songRequest.getTeam();
		return new SongDto.SongRequestResponse(
				songRequest.getId(),
				songRequest.getPerformance().getId(),
				team == null ? null : team.getId(),
				songRequest.getRequestedByMember().getId(),
				songRequest.getSong(),
				songRequest.getCreatedAt()
		);
	}

	private SongDto.SongVoteResponse toSongVoteResponse(SongVote songVote) {
		return new SongDto.SongVoteResponse(
				songVote.getId(),
				songVote.getSongRequest().getId(),
				songVote.getVoterMember().getId(),
				songVote.getVote(),
				songVote.getReason()
		);
	}
}
