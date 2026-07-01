package io.github.juc211.band_schedule.service;

import io.github.juc211.band_schedule.domain.Performance;
import io.github.juc211.band_schedule.domain.PerformanceMember;
import io.github.juc211.band_schedule.domain.Team;
import io.github.juc211.band_schedule.domain.TeamMember;
import io.github.juc211.band_schedule.dto.TeamDto;
import io.github.juc211.band_schedule.repository.PerformanceMemberRepository;
import io.github.juc211.band_schedule.repository.PerformanceRepository;
import io.github.juc211.band_schedule.repository.TeamMemberRepository;
import io.github.juc211.band_schedule.repository.TeamRepository;
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
	private final PerformanceMemberRepository performanceMemberRepository;
	private final TeamMemberRepository teamMemberRepository;

	public TeamDto.TeamResponse createTeam(Long performanceId, TeamDto.TeamCreateRequest request) {
		Performance performance = performanceRepository.findById(performanceId)
				.orElseThrow(() -> new IllegalArgumentException("Performance not found: " + performanceId));

		Team savedTeam = teamRepository.save(Team.create(performance, request.name(), request.confirmedSong()));

		return toTeamResponse(savedTeam);
	}

	public TeamDto.TeamResponse updateTeam(Long teamId, TeamDto.TeamUpdateRequest request) {
		Team team = teamRepository.findById(teamId)
				.orElseThrow(() -> new IllegalArgumentException("Team not found: " + teamId));

		team.update(request.name(), request.confirmedSong());

		return toTeamResponse(team);
	}

	@Transactional(readOnly = true)
	public List<TeamDto.TeamResponse> getTeamsByPerformance(Long performanceId) {
		validatePerformanceExists(performanceId);

		return teamRepository.findByPerformanceIdOrderByIdAsc(performanceId)
				.stream()
				.map(this::toTeamResponse)
				.toList();
	}

	public TeamDto.TeamMemberResponse addTeamMember(Long teamId, TeamDto.TeamMemberAddRequest request) {
		Team team = teamRepository.findById(teamId)
				.orElseThrow(() -> new IllegalArgumentException("Team not found: " + teamId));
		PerformanceMember performanceMember = performanceMemberRepository.findById(request.performanceMemberId())
				.orElseThrow(() -> new IllegalArgumentException("PerformanceMember not found: " + request.performanceMemberId()));

		validateSamePerformance(team, performanceMember);

		TeamMember savedTeamMember = teamMemberRepository.save(TeamMember.create(team, performanceMember, request.part()));

		return toTeamMemberResponse(savedTeamMember);
	}

	@Transactional(readOnly = true)
	public List<TeamDto.TeamMemberResponse> getTeamMembers(Long teamId) {
		validateTeamExists(teamId);

		return teamMemberRepository.findByTeamIdOrderByIdAsc(teamId)
				.stream()
				.map(this::toTeamMemberResponse)
				.toList();
	}

	private void validateSamePerformance(Team team, PerformanceMember performanceMember) {
		Long teamPerformanceId = team.getPerformance().getId();
		Long memberPerformanceId = performanceMember.getPerformance().getId();
		if (!teamPerformanceId.equals(memberPerformanceId)) {
			throw new IllegalArgumentException("PerformanceMember does not belong to team performance");
		}
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

	private TeamDto.TeamResponse toTeamResponse(Team team) {
		return new TeamDto.TeamResponse(
				team.getId(),
				team.getPerformance().getId(),
				team.getName(),
				team.getConfirmedSong()
		);
	}

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
