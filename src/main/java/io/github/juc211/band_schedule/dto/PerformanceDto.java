package io.github.juc211.band_schedule.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public abstract class PerformanceDto {
    public record PerformanceCreateRequest(
            @NotBlank(message = "공연 제목은 필수입니다.")
            @Size(max = 100, message = "공연 제목은 100자 이하여야 합니다.")
            String title,

            @NotNull(message = "공연일은 필수입니다.")
            LocalDate performanceDate,

            @NotBlank(message = "공연 장소는 필수입니다.")
            @Size(max = 100, message = "공연 장소는 100자 이하여야 합니다.")
            String location,
            LocalDate scheduleWindowStartDate,
            LocalDate scheduleWindowEndDate,
            Long clubId
    ) {
        public PerformanceCreateRequest(String title, LocalDate performanceDate, String location) {
            this(title, performanceDate, location, null, null, null);
        }

        public PerformanceCreateRequest(String title, LocalDate performanceDate, String location, LocalDate scheduleWindowStartDate, LocalDate scheduleWindowEndDate) {
            this(title, performanceDate, location, scheduleWindowStartDate, scheduleWindowEndDate, null);
        }

    }
    public record PerformanceCreateResponse(
            Long performanceId,
            String title
    ) {

    }

    public record PerformanceUpdateRequest(
            @NotBlank(message = "공연 제목은 필수입니다.")
            @Size(max = 100, message = "공연 제목은 100자 이하여야 합니다.")
            String title,

            @NotNull(message = "공연일은 필수입니다.")
            LocalDate performanceDate,

            @NotBlank(message = "공연 장소는 필수입니다.")
            @Size(max = 100, message = "공연 장소는 100자 이하여야 합니다.")
            String location,
            LocalDate scheduleWindowStartDate,
            LocalDate scheduleWindowEndDate
    ) {
        public PerformanceUpdateRequest(String title, LocalDate performanceDate, String location) {
            this(title, performanceDate, location, null, null);
        }

    }

    public record PerformanceScheduleWindowUpdateRequest(
            LocalDate scheduleWindowStartDate,
            LocalDate scheduleWindowEndDate
    ) {

    }

    public record PerformanceScheduleWindowResponse(
            Long performanceId,
            LocalDate scheduleWindowStartDate,
            LocalDate scheduleWindowEndDate
    ) {

    }

    public record PerformanceResponse(
            Long performanceId,
            String title,
            LocalDate performanceDate,
            String location,
            LocalDate scheduleWindowStartDate,
            LocalDate scheduleWindowEndDate
    ) {

    }

    public record PerformanceMemberAddRequest(
            @NotEmpty(message = "유저 ID 목록은 필수입니다.")
            List<@NotNull(message = "유저 ID는 필수입니다.") @Positive(message = "유저 ID는 양수여야 합니다.") Long> userIds
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
