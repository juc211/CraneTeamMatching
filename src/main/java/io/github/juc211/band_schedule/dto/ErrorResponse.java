package io.github.juc211.band_schedule.dto;

import java.time.LocalDateTime;

public record ErrorResponse(
        String code,          // 에러 식별 코드 (예: "TEAM_NOT_FOUND", "INVALID_INPUT")
        String message,       // 사용자 친화적 에러 메시지
        LocalDateTime timestamp
) {
    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(code, message, LocalDateTime.now());
    }

    public static ErrorResponse of(String message) {
        return new ErrorResponse("BAD_REQUEST", message, LocalDateTime.now());
    }
}
