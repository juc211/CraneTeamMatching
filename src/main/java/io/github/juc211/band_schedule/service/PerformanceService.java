package io.github.juc211.band_schedule.service;

import io.github.juc211.band_schedule.domain.Performance;
import io.github.juc211.band_schedule.domain.PerformanceMember;
import io.github.juc211.band_schedule.domain.User;
import io.github.juc211.band_schedule.domain.InputLink;
import io.github.juc211.band_schedule.domain.InputLinkType;
import io.github.juc211.band_schedule.dto.PerformanceDto;
import io.github.juc211.band_schedule.repository.AvailabilityRepository;
import io.github.juc211.band_schedule.repository.FinalScheduleRepository;
import io.github.juc211.band_schedule.repository.InputLinkRepository;
import io.github.juc211.band_schedule.repository.PerformanceConfirmedSongRepository;
import io.github.juc211.band_schedule.repository.PerformanceMemberRepository;
import io.github.juc211.band_schedule.repository.PerformanceRepository;
import io.github.juc211.band_schedule.repository.SongRequestRepository;
import io.github.juc211.band_schedule.repository.SongVoteRepository;
import io.github.juc211.band_schedule.repository.TeamMemberRepository;
import io.github.juc211.band_schedule.repository.TeamRepository;
import io.github.juc211.band_schedule.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PerformanceService {
    private final PerformanceRepository performanceRepository;
    private final PerformanceMemberRepository performanceMemberRepository;
    private final UserRepository userRepository;
    private final AvailabilityRepository availabilityRepository;
    private final FinalScheduleRepository finalScheduleRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final SongRequestRepository songRequestRepository;
    private final SongVoteRepository songVoteRepository;
    private final InputLinkRepository inputLinkRepository;
    private final PerformanceConfirmedSongRepository performanceConfirmedSongRepository;

    /**
     * 공연 생성
     */
    public PerformanceDto.PerformanceCreateResponse createPerformance(PerformanceDto.PerformanceCreateRequest request) {
        Performance performance = Performance.create(
                request.title(),
                request.performanceDate(),
                request.location(),
                request.scheduleWindowStartDate(),
                request.scheduleWindowEndDate()
        );

        Performance savedPerformance = performanceRepository.save(performance);

        return new PerformanceDto.PerformanceCreateResponse(
                savedPerformance.getId(),
                savedPerformance.getTitle()
        );
    }

    /**
     * 공연 목록 조회
     */
    @Transactional(readOnly = true)
    public List<PerformanceDto.PerformanceResponse> getPerformances() {
        return performanceRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .map(this::toPerformanceResponse)
                .toList();
    }

    /**
     * 공연 단건 조회
     */
    @Transactional(readOnly = true)
    public PerformanceDto.PerformanceResponse getPerformance(Long performanceId) {
        Performance performance = performanceRepository.findById(performanceId)
                .orElseThrow(() -> new IllegalArgumentException("Performance not found: " + performanceId));

        return toPerformanceResponse(performance);
    }

    /**
     * 링크 기반 공연 정보 조회
     */
    @Transactional(readOnly = true)
    public PerformanceDto.PerformanceResponse getPerformanceByLink(String token) {
        InputLink inputLink = getUsableLink(token);

        return toPerformanceResponse(inputLink.getPerformance());
    }

    /**
     * 공연 정보 수정
     */
    public PerformanceDto.PerformanceResponse updatePerformance(Long performanceId, PerformanceDto.PerformanceUpdateRequest request) {
        Performance performance = performanceRepository.findById(performanceId)
                .orElseThrow(() -> new IllegalArgumentException("Performance not found: " + performanceId));

        performance.update(
                request.title(),
                request.performanceDate(),
                request.location(),
                request.scheduleWindowStartDate(),
                request.scheduleWindowEndDate()
        );

        return toPerformanceResponse(performance);
    }

    /**
     * 공연 합주 기간 조회
     */
    @Transactional(readOnly = true)
    public PerformanceDto.PerformanceScheduleWindowResponse getPerformanceScheduleWindow(Long performanceId) {
        Performance performance = performanceRepository.findById(performanceId)
                .orElseThrow(() -> new IllegalArgumentException("Performance not found: " + performanceId));

        return toPerformanceScheduleWindowResponse(performance);
    }

    /**
     * 링크 기반 공연 합주 기간 조회
     */
    @Transactional(readOnly = true)
    public PerformanceDto.PerformanceScheduleWindowResponse getPerformanceScheduleWindowByLink(String token) {
        InputLink inputLink = getUsableLink(token);

        return toPerformanceScheduleWindowResponse(inputLink.getPerformance());
    }

    /**
     * 공연 합주 기간 지정 및 수정
     */
    public PerformanceDto.PerformanceResponse updatePerformanceScheduleWindow(
            Long performanceId,
            PerformanceDto.PerformanceScheduleWindowUpdateRequest request
    ) {
        Performance performance = performanceRepository.findById(performanceId)
                .orElseThrow(() -> new IllegalArgumentException("Performance not found: " + performanceId));

        performance.update(
                performance.getTitle(),
                performance.getPerformanceDate(),
                performance.getLocation(),
                request.scheduleWindowStartDate(),
                request.scheduleWindowEndDate()
        );

        return toPerformanceResponse(performance);
    }

    /**
     * 공연 합주 기간 삭제
     */
    public void deletePerformanceScheduleWindow(Long performanceId) {
        Performance performance = performanceRepository.findById(performanceId)
                .orElseThrow(() -> new IllegalArgumentException("Performance not found: " + performanceId));

        validateScheduleWindowCanBeDeleted(performanceId);

        performance.update(
                performance.getTitle(),
                performance.getPerformanceDate(),
                performance.getLocation(),
                null,
                null
        );
    }

    private void validateScheduleWindowCanBeDeleted(Long performanceId) {
        if (availabilityRepository.existsByTeamMemberTeamPerformanceId(performanceId)) {
            throw new IllegalArgumentException("Cannot delete schedule window because available times exist");
        }
        if (finalScheduleRepository.existsByTeamPerformanceId(performanceId)) {
            throw new IllegalArgumentException("Cannot delete schedule window because final schedules exist");
        }
    }

    /**
     * 공연 참여 인원 목록 조회
     */
    @Transactional(readOnly = true)
    public List<PerformanceDto.PerformanceMemberResponse> getPerformanceMembers(Long performanceId) {
        validatePerformanceExists(performanceId);

        return performanceMemberRepository.findByPerformanceIdOrderByIdAsc(performanceId)
                .stream()
                .map(this::toPerformanceMemberResponse)
                .toList();
    }

    /**
     * 링크 기반 공연 참여 인원 목록 조회
     */
    @Transactional(readOnly = true)
    public List<PerformanceDto.PerformanceMemberResponse> getPerformanceMembersByLink(String token) {
        InputLink inputLink = getUsableLink(token);
        validateLinkType(
                inputLink,
                InputLinkType.SONG_REQUEST,
                InputLinkType.SONG_VOTE,
                InputLinkType.AVAILABLE_TIME
        );

        return performanceMemberRepository.findByPerformanceIdOrderByIdAsc(inputLink.getPerformance().getId())
                .stream()
                .map(this::toPerformanceMemberResponse)
                .toList();
    }

    /**
     * 공연 참여 인원 추가
     */
    public PerformanceDto.PerformanceMemberAddResponse addPerformanceMembers(
            Long performanceId,
            PerformanceDto.PerformanceMemberAddRequest request
    ) {
        Performance performance = performanceRepository.findById(performanceId)
                .orElseThrow(() -> new IllegalArgumentException("Performance not found: " + performanceId));

        List<PerformanceMember> performanceMembers = new ArrayList<>();
        for (Long userId : request.userIds()) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
            performanceMembers.add(PerformanceMember.create(performance, user));
        }

        List<PerformanceMember> savedMembers = performanceMemberRepository.saveAll(performanceMembers);

        List<PerformanceDto.PerformanceMemberResponse> memberResponses = savedMembers.stream()
                .map(performanceMember -> new PerformanceDto.PerformanceMemberResponse(
                        performanceMember.getId(),
                        performanceMember.getUser().getId(),
                        performanceMember.getUser().getName()
                ))
                .toList();

        return new PerformanceDto.PerformanceMemberAddResponse(performance.getId(), memberResponses);
    }

    /**
     * 공연 참여 인원 삭제
     */
    public void deletePerformanceMember(Long performanceMemberId) {
        PerformanceMember performanceMember = performanceMemberRepository.findById(performanceMemberId)
                .orElseThrow(() -> new IllegalArgumentException("PerformanceMember not found: " + performanceMemberId));

        songVoteRepository.deleteBySongRequestRequestedByMemberId(performanceMemberId);
        songRequestRepository.deleteByRequestedByMemberId(performanceMemberId);
        songVoteRepository.deleteByVoterMemberId(performanceMemberId);
        availabilityRepository.deleteByTeamMemberPerformanceMemberId(performanceMemberId);
        teamMemberRepository.deleteByPerformanceMemberId(performanceMemberId);
        performanceMemberRepository.delete(performanceMember);
    }

    /**
     * 공연 삭제
     */
    public void deletePerformance(Long performanceId) {
        Performance performance = performanceRepository.findById(performanceId)
                .orElseThrow(() -> new IllegalArgumentException("Performance not found: " + performanceId));

        inputLinkRepository.deleteByPerformanceId(performanceId);
        performanceConfirmedSongRepository.deleteByPerformanceId(performanceId);
        finalScheduleRepository.deleteByTeamPerformanceId(performanceId);
        songVoteRepository.deleteBySongRequestPerformanceId(performanceId);
        songRequestRepository.deleteByPerformanceId(performanceId);
        availabilityRepository.deleteByTeamMemberTeamPerformanceId(performanceId);
        teamMemberRepository.deleteByTeamPerformanceId(performanceId);
        teamRepository.deleteByPerformanceId(performanceId);
        performanceMemberRepository.deleteByPerformanceId(performanceId);
        performanceRepository.delete(performance);
    }

    private PerformanceDto.PerformanceResponse toPerformanceResponse(Performance performance) {
        return new PerformanceDto.PerformanceResponse(
                performance.getId(),
                performance.getTitle(),
                performance.getPerformanceDate(),
                performance.getLocation(),
                performance.getScheduleWindowStartDate(),
                performance.getScheduleWindowEndDate()
        );
    }

    private PerformanceDto.PerformanceScheduleWindowResponse toPerformanceScheduleWindowResponse(Performance performance) {
        return new PerformanceDto.PerformanceScheduleWindowResponse(
                performance.getId(),
                performance.getScheduleWindowStartDate(),
                performance.getScheduleWindowEndDate()
        );
    }

    private PerformanceDto.PerformanceMemberResponse toPerformanceMemberResponse(PerformanceMember performanceMember) {
        return new PerformanceDto.PerformanceMemberResponse(
                performanceMember.getId(),
                performanceMember.getUser().getId(),
                performanceMember.getUser().getName()
        );
    }

    private void validatePerformanceExists(Long performanceId) {
        if (!performanceRepository.existsById(performanceId)) {
            throw new IllegalArgumentException("Performance not found: " + performanceId);
        }
    }

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

    private void validateLinkType(InputLink inputLink, InputLinkType... expectedTypes) {
        for (InputLinkType expectedType : expectedTypes) {
            if (inputLink.getType() == expectedType) {
                return;
            }
        }
        throw new IllegalArgumentException("InputLink type is not allowed");
    }
}
