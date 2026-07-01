package io.github.juc211.band_schedule.controller;

import io.github.juc211.band_schedule.dto.PerformanceDto;
import io.github.juc211.band_schedule.service.PerformanceService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/performances")
public class PerformanceController {
    private final PerformanceService performanceService;

    /**
     * 공연 생성
     */
    @PostMapping
    public ResponseEntity<PerformanceDto.PerformanceCreateResponse> createPerformance(@RequestBody PerformanceDto.PerformanceCreateRequest request) {
        PerformanceDto.PerformanceCreateResponse response = performanceService.createPerformance(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 공연 목록 조회
     */
    @GetMapping
    public ResponseEntity<List<PerformanceDto.PerformanceResponse>> getPerformances() {
        return ResponseEntity.ok(performanceService.getPerformances());
    }

    /**
     * 공연 단건 조회
     */
    @GetMapping("/{performanceId}")
    public ResponseEntity<PerformanceDto.PerformanceResponse> getPerformance(@PathVariable Long performanceId) {
        return ResponseEntity.ok(performanceService.getPerformance(performanceId));
    }

    /**
     * 공연 수정
     */
    @PatchMapping("/{performanceId}")
    public ResponseEntity<PerformanceDto.PerformanceResponse> updatePerformance(
            @PathVariable Long performanceId,
            @RequestBody PerformanceDto.PerformanceUpdateRequest request
    ) {
        return ResponseEntity.ok(performanceService.updatePerformance(performanceId, request));
    }

    /**
     * 공연 참여 인원 추가
     */
    @PostMapping("/{performanceId}/members")
    public ResponseEntity<PerformanceDto.PerformanceMemberAddResponse> addPerformanceMembers(
            @PathVariable Long performanceId,
            @RequestBody PerformanceDto.PerformanceMemberAddRequest request
    ) {
        PerformanceDto.PerformanceMemberAddResponse response = performanceService.addPerformanceMembers(performanceId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 공연 참여 인원 목록 조회
     */
    @GetMapping("/{performanceId}/members")
    public ResponseEntity<List<PerformanceDto.PerformanceMemberResponse>> getPerformanceMembers(@PathVariable Long performanceId) {
        return ResponseEntity.ok(performanceService.getPerformanceMembers(performanceId));
    }

}
