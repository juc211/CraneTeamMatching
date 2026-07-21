package io.github.juc211.band_schedule.service;

import io.github.juc211.band_schedule.domain.InputLink;
import io.github.juc211.band_schedule.domain.InputLinkType;
import io.github.juc211.band_schedule.domain.PerformanceMember;
import io.github.juc211.band_schedule.domain.SongRequest;
import io.github.juc211.band_schedule.domain.SongVote;
import io.github.juc211.band_schedule.domain.Team;
import io.github.juc211.band_schedule.dto.SongDto;
import io.github.juc211.band_schedule.exception.BusinessException;
import io.github.juc211.band_schedule.exception.ErrorCode;
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
				.orElseThrow(() -> new BusinessException(ErrorCode.INPUT_LINK_NOT_FOUND, "InputLink not found: " + token));
		PerformanceMember requestedByMember = performanceMemberRepository.findById(request.requestedByMemberId())
				.orElseThrow(() -> new BusinessException(ErrorCode.PERFORMANCE_MEMBER_NOT_FOUND, "PerformanceMember not found: " + request.requestedByMemberId()));

		validateUsableLink(inputLink);
		validateLinkType(inputLink, InputLinkType.SONG_REQUEST);
		validateSamePerformance(inputLink, requestedByMember);
		Team selectedTeam = getSelectedTeam(request.teamId());
		validateTeamBelongsToLinkPerformance(inputLink, selectedTeam);

		SongRequest savedSongRequest = songRequestRepository.save(SongRequest.create(
				inputLink.getPerformance(),
				selectedTeam,
				requestedByMember,
				request.song(),
				request.youtubeUrl()
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
				.orElseThrow(() -> new BusinessException(ErrorCode.INPUT_LINK_NOT_FOUND, "InputLink not found: " + token));

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
				.orElseThrow(() -> new BusinessException(ErrorCode.INPUT_LINK_NOT_FOUND, "InputLink not found: " + token));
		Team team = teamRepository.findById(teamId)
				.orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_FOUND, "Team not found: " + teamId));

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
				.orElseThrow(() -> new BusinessException(ErrorCode.SONG_REQUEST_NOT_FOUND, "SongRequest not found: " + songRequestId));
		Team selectedTeam = getSelectedTeam(request.teamId());

		validateTeamBelongsToSongRequestPerformance(songRequest, selectedTeam);

		songRequest.update(selectedTeam, request.song(), request.youtubeUrl());

		return toSongRequestResponse(songRequest);
	}

	/**
	 * 희망곡 삭제
	 */
	public void deleteSongRequest(Long songRequestId) {
		SongRequest songRequest = songRequestRepository.findById(songRequestId)
				.orElseThrow(() -> new BusinessException(ErrorCode.SONG_REQUEST_NOT_FOUND, "SongRequest not found: " + songRequestId));

		songVoteRepository.deleteBySongRequestId(songRequestId);
		songRequestRepository.delete(songRequest);
	}

	/**
	 * 희망곡 투표 삭제
	 */
	public void deleteSongVote(Long songVoteId) {
		SongVote songVote = songVoteRepository.findById(songVoteId)
				.orElseThrow(() -> new BusinessException(ErrorCode.SONG_VOTE_NOT_FOUND, "SongVote not found: " + songVoteId));

		songVoteRepository.delete(songVote);
	}

	/**
	 * 희망곡 투표 제출
	 */
	public SongDto.SongVoteResponse submitSongVote(String token, SongDto.SongVoteSubmitRequest request) {
		InputLink inputLink = inputLinkRepository.findByToken(token)
				.orElseThrow(() -> new BusinessException(ErrorCode.INPUT_LINK_NOT_FOUND, "InputLink not found: " + token));
		SongRequest songRequest = songRequestRepository.findById(request.songRequestId())
				.orElseThrow(() -> new BusinessException(ErrorCode.SONG_REQUEST_NOT_FOUND, "SongRequest not found: " + request.songRequestId()));
		PerformanceMember voterMember = performanceMemberRepository.findById(request.voterMemberId())
				.orElseThrow(() -> new BusinessException(ErrorCode.PERFORMANCE_MEMBER_NOT_FOUND, "PerformanceMember not found: " + request.voterMemberId()));

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

	/**
	 * 링크 사용 가능 여부 검증
	 */
	private void validateUsableLink(InputLink inputLink) {
		if (!inputLink.isActive()) {
			throw new BusinessException(ErrorCode.INPUT_LINK_INACTIVE, "InputLink is inactive");
		}
		if (inputLink.getExpiresAt() != null && inputLink.getExpiresAt().isBefore(LocalDateTime.now())) {
			throw new BusinessException(ErrorCode.LINK_EXPIRED, "InputLink is expired");
		}
	}

	/**
	 * 링크 타입 검증
	 */
	private void validateLinkType(InputLink inputLink, InputLinkType expectedType) {
		if (inputLink.getType() != expectedType) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_LINK_TYPE, "InputLink type must be " + expectedType);
		}
	}

	/**
	 * 링크 공연과 공연 참여 인원 공연 일치 여부 검증
	 */
	private void validateSamePerformance(InputLink inputLink, PerformanceMember requestedByMember) {
		Long linkPerformanceId = inputLink.getPerformance().getId();
		Long memberPerformanceId = requestedByMember.getPerformance().getId();
		if (!linkPerformanceId.equals(memberPerformanceId)) {
			throw new BusinessException(ErrorCode.PERFORMANCE_MEMBER_NOT_IN_PERFORMANCE, "PerformanceMember does not belong to link performance");
		}
	}

	/**
	 * 선택된 팀 조회
	 */
	private Team getSelectedTeam(Long teamId) {
		if (teamId == null) {
			return null;
		}

		return teamRepository.findById(teamId)
				.orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_FOUND, "Team not found: " + teamId));
	}

	/**
	 * 공연 존재 여부 검증
	 */
	private void validatePerformanceExists(Long performanceId) {
		if (!performanceRepository.existsById(performanceId)) {
			throw new BusinessException(ErrorCode.PERFORMANCE_NOT_FOUND, "Performance not found: " + performanceId);
		}
	}

	/**
	 * 팀 존재 여부 검증
	 */
	private void validateTeamExists(Long teamId) {
		if (!teamRepository.existsById(teamId)) {
			throw new BusinessException(ErrorCode.TEAM_NOT_FOUND, "Team not found: " + teamId);
		}
	}

	/**
	 * 희망곡 신청 존재 여부 검증
	 */
	private void validateSongRequestExists(Long songRequestId) {
		if (!songRequestRepository.existsById(songRequestId)) {
			throw new BusinessException(ErrorCode.SONG_REQUEST_NOT_FOUND, "SongRequest not found: " + songRequestId);
		}
	}

	/**
	 * 팀이 링크 공연에 속하는지 검증
	 */
	private void validateTeamBelongsToLinkPerformance(InputLink inputLink, Team selectedTeam) {
		if (selectedTeam == null) {
			return;
		}

		Long linkPerformanceId = inputLink.getPerformance().getId();
		Long teamPerformanceId = selectedTeam.getPerformance().getId();
		if (!linkPerformanceId.equals(teamPerformanceId)) {
			throw new BusinessException(ErrorCode.TEAM_NOT_IN_LINK_PERFORMANCE, "Team does not belong to link performance");
		}
	}

	/**
	 * 팀이 희망곡 신청 공연에 속하는지 검증
	 */
	private void validateTeamBelongsToSongRequestPerformance(SongRequest songRequest, Team selectedTeam) {
		if (selectedTeam == null) {
			return;
		}

		Long songRequestPerformanceId = songRequest.getPerformance().getId();
		Long teamPerformanceId = selectedTeam.getPerformance().getId();
		if (!songRequestPerformanceId.equals(teamPerformanceId)) {
			throw new BusinessException(ErrorCode.TEAM_NOT_IN_SONG_REQUEST_PERFORMANCE, "Team does not belong to song request performance");
		}
	}

	/**
	 * 희망곡 신청이 링크 공연에 속하는지 검증
	 */
	private void validateSongRequestBelongsToLinkPerformance(InputLink inputLink, SongRequest songRequest) {
		Long linkPerformanceId = inputLink.getPerformance().getId();
		Long songRequestPerformanceId = songRequest.getPerformance().getId();
		if (!linkPerformanceId.equals(songRequestPerformanceId)) {
			throw new BusinessException(ErrorCode.SONG_REQUEST_NOT_IN_LINK_PERFORMANCE, "SongRequest does not belong to link performance");
		}
	}

	/**
	 * 희망곡 신청 응답 변환
	 */
	private SongDto.SongRequestResponse toSongRequestResponse(SongRequest songRequest) {
		Team team = songRequest.getTeam();
		return new SongDto.SongRequestResponse(
				songRequest.getId(),
				songRequest.getPerformance().getId(),
				team == null ? null : team.getId(),
				songRequest.getRequestedByMember().getId(),
				songRequest.getSong(),
				songRequest.getYoutubeUrl(),
				songRequest.getCreatedAt()
		);
	}

	/**
	 * 희망곡 투표 응답 변환
	 */
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
