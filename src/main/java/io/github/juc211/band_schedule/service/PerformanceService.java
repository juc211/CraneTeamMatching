package io.github.juc211.band_schedule.service;

import io.github.juc211.band_schedule.domain.Performance;
import io.github.juc211.band_schedule.domain.PerformanceMember;
import io.github.juc211.band_schedule.domain.User;
import io.github.juc211.band_schedule.dto.PerformanceDto;
import io.github.juc211.band_schedule.repository.PerformanceMemberRepository;
import io.github.juc211.band_schedule.repository.PerformanceRepository;
import io.github.juc211.band_schedule.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
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

    public PerformanceDto.PerformanceCreateResponse createPerformance(PerformanceDto.PerformanceCreateRequest request) {
        Performance performance = Performance.create(
                request.title(),
                request.performanceDate(),
                request.location()
        );

        Performance savedPerformance = performanceRepository.save(performance);

        return new PerformanceDto.PerformanceCreateResponse(
                savedPerformance.getId(),
                savedPerformance.getTitle()
        );
    }

    @Transactional(readOnly = true)
    public List<PerformanceDto.PerformanceResponse> getPerformances() {
        return performanceRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .map(this::toPerformanceResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PerformanceDto.PerformanceResponse getPerformance(Long performanceId) {
        Performance performance = performanceRepository.findById(performanceId)
                .orElseThrow(() -> new IllegalArgumentException("Performance not found: " + performanceId));

        return toPerformanceResponse(performance);
    }

    public PerformanceDto.PerformanceResponse updatePerformance(Long performanceId, PerformanceDto.PerformanceUpdateRequest request) {
        Performance performance = performanceRepository.findById(performanceId)
                .orElseThrow(() -> new IllegalArgumentException("Performance not found: " + performanceId));

        performance.update(request.title(), request.performanceDate(), request.location());

        return toPerformanceResponse(performance);
    }

    @Transactional(readOnly = true)
    public List<PerformanceDto.PerformanceMemberResponse> getPerformanceMembers(Long performanceId) {
        validatePerformanceExists(performanceId);

        return performanceMemberRepository.findByPerformanceIdOrderByIdAsc(performanceId)
                .stream()
                .map(this::toPerformanceMemberResponse)
                .toList();
    }

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

    private PerformanceDto.PerformanceResponse toPerformanceResponse(Performance performance) {
        return new PerformanceDto.PerformanceResponse(
                performance.getId(),
                performance.getTitle(),
                performance.getPerformanceDate(),
                performance.getLocation()
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
}
