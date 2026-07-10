package io.github.juc211.band_schedule.service;

import io.github.juc211.band_schedule.domain.PerformanceConfirmedSong;
import io.github.juc211.band_schedule.domain.InputLink;
import io.github.juc211.band_schedule.domain.InputLinkType;
import io.github.juc211.band_schedule.domain.Performance;
import io.github.juc211.band_schedule.domain.PerformanceMember;
import io.github.juc211.band_schedule.domain.Team;
import io.github.juc211.band_schedule.domain.TeamMember;
import io.github.juc211.band_schedule.dto.TeamDto;
import io.github.juc211.band_schedule.repository.InputLinkRepository;
import io.github.juc211.band_schedule.repository.AvailabilityRepository;
import io.github.juc211.band_schedule.repository.FinalScheduleRepository;
import io.github.juc211.band_schedule.repository.PerformanceConfirmedSongRepository;
import io.github.juc211.band_schedule.repository.PerformanceMemberRepository;
import io.github.juc211.band_schedule.repository.PerformanceRepository;
import io.github.juc211.band_schedule.repository.PerformanceSetlistItemRepository;
import io.github.juc211.band_schedule.repository.SongRequestRepository;
import io.github.juc211.band_schedule.repository.SongVoteRepository;
import io.github.juc211.band_schedule.repository.TeamMemberRepository;
import io.github.juc211.band_schedule.repository.TeamRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TeamService {

	private final PerformanceRepository performanceRepository;
	private final TeamRepository teamRepository;
	private final PerformanceConfirmedSongRepository performanceConfirmedSongRepository;
	private final PerformanceMemberRepository performanceMemberRepository;
	private final TeamMemberRepository teamMemberRepository;
	private final InputLinkRepository inputLinkRepository;
	private final AvailabilityRepository availabilityRepository;
	private final FinalScheduleRepository finalScheduleRepository;
	private final SongRequestRepository songRequestRepository;
	private final SongVoteRepository songVoteRepository;
	private final PerformanceSetlistItemRepository performanceSetlistItemRepository;

	/**
	 * 팀 생성
	 */
	public TeamDto.TeamResponse createTeam(Long performanceId, TeamDto.TeamCreateRequest request) {
		Performance performance = performanceRepository.findById(performanceId)
				.orElseThrow(() -> new IllegalArgumentException("Performance not found: " + performanceId));

		String confirmedSong = resolvePerformanceConfirmedSong(performance.getId(), request.confirmedSong(), request.performanceConfirmedSongId());
		Team savedTeam = teamRepository.save(Team.create(performance, request.name(), confirmedSong));

		return toTeamResponse(savedTeam);
	}

	/**
	 * 팀 정보 수정
	 */
	public TeamDto.TeamResponse updateTeam(Long teamId, TeamDto.TeamUpdateRequest request) {
		Team team = teamRepository.findById(teamId)
				.orElseThrow(() -> new IllegalArgumentException("Team not found: " + teamId));

		String confirmedSong = resolvePerformanceConfirmedSong(team.getPerformance().getId(), request.confirmedSong(), request.performanceConfirmedSongId());
		team.update(request.name(), confirmedSong);

		return toTeamResponse(team);
	}

	/**
	 * 팀 단위 확정곡 조회
	 */
	@Transactional(readOnly = true)
	public TeamDto.TeamConfirmedSongResponse getTeamConfirmedSong(Long teamId) {
		Team team = teamRepository.findById(teamId)
				.orElseThrow(() -> new IllegalArgumentException("Team not found: " + teamId));

		return toTeamConfirmedSongResponse(team);
	}

	/**
	 * 팀 단위 확정곡 지정 및 수정
	 */
	public TeamDto.TeamResponse updateTeamConfirmedSong(Long teamId, TeamDto.TeamConfirmedSongUpdateRequest request) {
		Team team = teamRepository.findById(teamId)
				.orElseThrow(() -> new IllegalArgumentException("Team not found: " + teamId));

		String confirmedSong = resolvePerformanceConfirmedSong(team.getPerformance().getId(), request.confirmedSong(), request.performanceConfirmedSongId());
		team.updateConfirmedSong(confirmedSong);

		return toTeamResponse(team);
	}

	/**
	 * 팀 단위 확정곡 삭제
	 */
	public void deleteTeamConfirmedSong(Long teamId) {
		Team team = teamRepository.findById(teamId)
				.orElseThrow(() -> new IllegalArgumentException("Team not found: " + teamId));

		team.updateConfirmedSong(null);
	}

	/**
	 * 공연별 팀 목록 조회
	 */
	@Transactional(readOnly = true)
	public List<TeamDto.TeamResponse> getTeamsByPerformance(Long performanceId) {
		validatePerformanceExists(performanceId);

		return teamRepository.findByPerformanceIdOrderByIdAsc(performanceId)
				.stream()
				.map(this::toTeamResponse)
				.toList();
	}

	/**
	 * 링크 기반 공연 팀 목록 조회
	 */
	@Transactional(readOnly = true)
	public List<TeamDto.TeamResponse> getTeamsByLink(String token) {
		InputLink inputLink = getUsableLink(token);
		validateLinkType(
				inputLink,
				InputLinkType.SONG_REQUEST,
				InputLinkType.SONG_VOTE,
				InputLinkType.AVAILABLE_TIME,
				InputLinkType.FINAL_SCHEDULE_VIEW
		);

		return teamRepository.findByPerformanceIdOrderByIdAsc(inputLink.getPerformance().getId())
				.stream()
				.map(this::toTeamResponse)
				.toList();
	}

	/**
	 * 팀원 추가
	 */
	public TeamDto.TeamMemberResponse addTeamMember(Long teamId, TeamDto.TeamMemberAddRequest request) {
		Team team = teamRepository.findById(teamId)
				.orElseThrow(() -> new IllegalArgumentException("Team not found: " + teamId));
		PerformanceMember performanceMember = performanceMemberRepository.findById(request.performanceMemberId())
				.orElseThrow(() -> new IllegalArgumentException("PerformanceMember not found: " + request.performanceMemberId()));

		validateSamePerformance(team, performanceMember);

		TeamMember savedTeamMember = teamMemberRepository.save(TeamMember.create(team, performanceMember, request.part()));

		return toTeamMemberResponse(savedTeamMember);
	}

	/**
	 * 팀원 파트 수정
	 */
	public TeamDto.TeamMemberResponse updateTeamMember(Long teamMemberId, TeamDto.TeamMemberUpdateRequest request) {
		TeamMember teamMember = teamMemberRepository.findById(teamMemberId)
				.orElseThrow(() -> new IllegalArgumentException("TeamMember not found: " + teamMemberId));

		teamMember.updatePart(request.part());

		return toTeamMemberResponse(teamMember);
	}

	/**
	 * 팀 내부 팀원 목록 조회
	 */
	@Transactional(readOnly = true)
	public List<TeamDto.TeamMemberResponse> getTeamMembers(Long teamId) {
		validateTeamExists(teamId);

		return teamMemberRepository.findByTeamIdOrderByIdAsc(teamId)
				.stream()
				.map(this::toTeamMemberResponse)
				.toList();
	}

	/**
	 * 링크 기반 팀 내부 팀원 목록 조회
	 */
	@Transactional(readOnly = true)
	public List<TeamDto.TeamMemberResponse> getTeamMembersByLink(String token, Long teamId) {
		InputLink inputLink = getUsableLink(token);
		validateLinkType(inputLink, InputLinkType.AVAILABLE_TIME);

		Team team = teamRepository.findById(teamId)
				.orElseThrow(() -> new IllegalArgumentException("Team not found: " + teamId));
		validateTeamBelongsToLinkPerformance(inputLink, team);

		return teamMemberRepository.findByTeamIdOrderByIdAsc(teamId)
				.stream()
				.map(this::toTeamMemberResponse)
				.toList();
	}

	/**
	 * 팀원 삭제
	 */
	public void deleteTeamMember(Long teamMemberId) {
		TeamMember teamMember = teamMemberRepository.findById(teamMemberId)
				.orElseThrow(() -> new IllegalArgumentException("TeamMember not found: " + teamMemberId));

		availabilityRepository.deleteByTeamMemberId(teamMemberId);
		teamMemberRepository.delete(teamMember);
	}

	/**
	 * 팀 삭제
	 */
	public void deleteTeam(Long teamId) {
		Team team = teamRepository.findById(teamId)
				.orElseThrow(() -> new IllegalArgumentException("Team not found: " + teamId));

		finalScheduleRepository.deleteByTeamId(teamId);
		performanceSetlistItemRepository.deleteByTeamId(teamId);
		songVoteRepository.deleteBySongRequestTeamId(teamId);
		songRequestRepository.deleteByTeamId(teamId);
		availabilityRepository.deleteByTeamMemberTeamId(teamId);
		teamMemberRepository.deleteByTeamId(teamId);
		teamRepository.delete(team);
	}

	/**
	 * 팀 공연과 공연 참여 인원 공연 일치 여부 검증
	 */
	private void validateSamePerformance(Team team, PerformanceMember performanceMember) {
		Long teamPerformanceId = team.getPerformance().getId();
		Long memberPerformanceId = performanceMember.getPerformance().getId();
		if (!teamPerformanceId.equals(memberPerformanceId)) {
			throw new IllegalArgumentException("PerformanceMember does not belong to team performance");
		}
	}

	/**
	 * 공연 단위 확정곡 참조를 팀 확정곡 문자열로 변환
	 */
	private String resolvePerformanceConfirmedSong(Long performanceId, String confirmedSong, Long performanceConfirmedSongId) {
		if (performanceConfirmedSongId == null) {
			return confirmedSong;
		}

		PerformanceConfirmedSong savedPerformanceConfirmedSong = performanceConfirmedSongRepository.findById(performanceConfirmedSongId)
				.orElseThrow(() -> new IllegalArgumentException("PerformanceConfirmedSong not found: " + performanceConfirmedSongId));

		if (!savedPerformanceConfirmedSong.getPerformance().getId().equals(performanceId)) {
			throw new IllegalArgumentException("PerformanceConfirmedSong does not belong to team performance");
		}

		return savedPerformanceConfirmedSong.getSong();
	}

	/**
	 * 공연 존재 여부 검증
	 */
	private void validatePerformanceExists(Long performanceId) {
		if (!performanceRepository.existsById(performanceId)) {
			throw new IllegalArgumentException("Performance not found: " + performanceId);
		}
	}

	/**
	 * 팀 존재 여부 검증
	 */
	private void validateTeamExists(Long teamId) {
		if (!teamRepository.existsById(teamId)) {
			throw new IllegalArgumentException("Team not found: " + teamId);
		}
	}

	/**
	 * 사용 가능한 링크 조회
	 */
	private InputLink getUsableLink(String token) {
		InputLink inputLink = inputLinkRepository.findByToken(token)
				.orElseThrow(() -> new IllegalArgumentException("InputLink not found: " + token));
		if (!inputLink.isActive()) {
			throw new IllegalArgumentException("InputLink is inactive");
		}
		if (inputLink.getExpiresAt() != null && inputLink.getExpiresAt().isBefore(LocalDateTime.now())) {
			throw new IllegalArgumentException("InputLink is expired");
		}
		return inputLink;
	}

	/**
	 * 링크 타입 검증
	 */
	private void validateLinkType(InputLink inputLink, InputLinkType... expectedTypes) {
		for (InputLinkType expectedType : expectedTypes) {
			if (inputLink.getType() == expectedType) {
				return;
			}
		}
		throw new IllegalArgumentException("InputLink type is not allowed");
	}

	/**
	 * 팀이 링크 공연에 속하는지 검증
	 */
	private void validateTeamBelongsToLinkPerformance(InputLink inputLink, Team team) {
		Long linkPerformanceId = inputLink.getPerformance().getId();
		Long teamPerformanceId = team.getPerformance().getId();
		if (!linkPerformanceId.equals(teamPerformanceId)) {
			throw new IllegalArgumentException("Team does not belong to link performance");
		}
	}

	/**
	 * 팀 응답 변환
	 */
	private TeamDto.TeamResponse toTeamResponse(Team team) {
		return new TeamDto.TeamResponse(
				team.getId(),
				team.getPerformance().getId(),
				team.getName(),
				team.getConfirmedSong()
		);
	}

	/**
	 * 팀 확정곡 응답 변환
	 */
	private TeamDto.TeamConfirmedSongResponse toTeamConfirmedSongResponse(Team team) {
		return new TeamDto.TeamConfirmedSongResponse(
				team.getId(),
				team.getConfirmedSong()
		);
	}

	/**
	 * 팀원 응답 변환
	 */
	private TeamDto.TeamMemberResponse toTeamMemberResponse(TeamMember teamMember) {
		return new TeamDto.TeamMemberResponse(
				teamMember.getId(),
				teamMember.getTeam().getId(),
				teamMember.getPerformanceMember().getId(),
				teamMember.getPerformanceMember().getUser().getId(),
				teamMember.getPerformanceMember().getUser().getName(),
				teamMember.getPart()
		);
	}
}
