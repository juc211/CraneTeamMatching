package io.github.juc211.band_schedule.dto;

import java.time.LocalDate;
import java.util.List;

public abstract class PerformanceDto {
    public record PerformanceCreateRequest(
            String title,
            LocalDate performanceDate,
            String location
    ) {

    }
    public record PerformanceCreateResponse(
            Long performanceId,
            String title
    ) {

    }

    public record PerformanceUpdateRequest(
            String title,
            LocalDate performanceDate,
            String location
    ) {

    }

    public record PerformanceResponse(
            Long performanceId,
            String title,
            LocalDate performanceDate,
            String location
    ) {

    }

    public record PerformanceMemberAddRequest(
            List<Long> userIds
    ) {

    }

    public record PerformanceMemberResponse(
            Long performanceMemberId,
            Long userId,
            String name
    ) {

    }

    public record PerformanceMemberAddResponse(
            Long performanceId,
            List<PerformanceMemberResponse> members
    ) {

    }
}
