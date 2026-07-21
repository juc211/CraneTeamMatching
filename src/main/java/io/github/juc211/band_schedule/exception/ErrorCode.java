package io.github.juc211.band_schedule.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // 400 Bad Request
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "적절하지 않은 요청 값입니다."),
    REQUIRED_VALUE_MISSING(HttpStatus.BAD_REQUEST, "C003", "필수 요청 값이 누락되었습니다."),
    DUPLICATE_VALUE(HttpStatus.BAD_REQUEST, "C004", "중복된 요청 값입니다."),

    // 400 Bad Request - Link
    INPUT_LINK_TYPE_REQUIRED(HttpStatus.BAD_REQUEST, "L000", "링크 타입은 필수입니다."),
    INPUT_LINK_INACTIVE(HttpStatus.BAD_REQUEST, "L002", "비활성화된 접근 링크입니다."),
    LINK_EXPIRED(HttpStatus.BAD_REQUEST, "L001", "만료된 접근 링크입니다."),
    INVALID_INPUT_LINK_TYPE(HttpStatus.BAD_REQUEST, "L003", "허용되지 않는 링크 타입입니다."),
    INPUT_LINK_NOT_FOR_MEMBER_INPUT(HttpStatus.BAD_REQUEST, "L004", "멤버 입력용 링크가 아닙니다."),

    // 400 Bad Request - Schedule window
    SCHEDULE_WINDOW_REQUIRED(HttpStatus.BAD_REQUEST, "SW001", "공연 합주 기간이 필요합니다."),
    SCHEDULE_WINDOW_DATES_REQUIRED_TOGETHER(HttpStatus.BAD_REQUEST, "SW002", "합주 기간 시작일과 종료일은 함께 입력해야 합니다."),
    SCHEDULE_WINDOW_START_AFTER_END(HttpStatus.BAD_REQUEST, "SW003", "합주 기간 시작일은 종료일보다 늦을 수 없습니다."),
    SCHEDULE_WINDOW_END_AFTER_PERFORMANCE_DATE(HttpStatus.BAD_REQUEST, "SW004", "합주 기간 종료일은 공연일보다 늦을 수 없습니다."),
    SCHEDULE_WINDOW_DELETE_BLOCKED_BY_AVAILABLE_TIMES(HttpStatus.BAD_REQUEST, "SW005", "가능 시간이 있어 합주 기간을 삭제할 수 없습니다."),
    SCHEDULE_WINDOW_DELETE_BLOCKED_BY_FINAL_SCHEDULES(HttpStatus.BAD_REQUEST, "SW006", "최종 합주 일정이 있어 합주 기간을 삭제할 수 없습니다."),

    // 400 Bad Request - Available time
    AVAILABLE_TIME_DATES_REQUIRED_TOGETHER(HttpStatus.BAD_REQUEST, "AT001", "가능 시간 시작/종료 시간은 함께 입력해야 합니다."),
    AVAILABLE_TIME_START_NOT_BEFORE_END(HttpStatus.BAD_REQUEST, "AT002", "가능 시간 시작은 종료보다 빨라야 합니다."),
    AVAILABLE_TIME_OUT_OF_SCHEDULE_WINDOW(HttpStatus.BAD_REQUEST, "AT003", "가능 시간은 공연 합주 기간 안에 있어야 합니다."),
    TEAM_MEMBER_NOT_IN_LINK_PERFORMANCE(HttpStatus.BAD_REQUEST, "AT004", "팀원이 링크 공연에 속하지 않습니다."),

    // 400 Bad Request - Final schedule
    FINAL_SCHEDULE_DATES_REQUIRED_TOGETHER(HttpStatus.BAD_REQUEST, "FS001", "최종 합주 일정 시작/종료 시간은 함께 입력해야 합니다."),
    FINAL_SCHEDULE_START_NOT_BEFORE_END(HttpStatus.BAD_REQUEST, "FS002", "최종 합주 일정 시작은 종료보다 빨라야 합니다."),
    FINAL_SCHEDULE_OVERLAPPED(HttpStatus.BAD_REQUEST, "FS003", "최종 합주 일정이 다른 일정과 겹칩니다."),
    TEAM_NOT_IN_LINK_PERFORMANCE(HttpStatus.BAD_REQUEST, "T002", "팀이 링크 공연에 속하지 않습니다."),

    // 400 Bad Request - User
    USER_STATUS_REQUIRED(HttpStatus.BAD_REQUEST, "U002", "유저 상태는 필수입니다."),
    USER_HAS_REFERENCES(HttpStatus.BAD_REQUEST, "U003", "참조가 있는 유저는 삭제할 수 없습니다."),
    STUDENT_NUMBER_DUPLICATED(HttpStatus.BAD_REQUEST, "U004", "이미 존재하는 학번입니다."),

    // 400 Bad Request - Performance member
    USER_IDS_REQUIRED(HttpStatus.BAD_REQUEST, "PM002", "유저 ID 목록은 필수입니다."),
    USER_ID_REQUIRED(HttpStatus.BAD_REQUEST, "PM003", "유저 ID는 필수입니다."),
    DUPLICATE_USER_ID_IN_REQUEST(HttpStatus.BAD_REQUEST, "PM004", "공연 참여 인원 요청에 중복된 유저 ID가 있습니다."),
    USER_ALREADY_ADDED_TO_PERFORMANCE(HttpStatus.BAD_REQUEST, "PM005", "이미 공연에 추가된 유저입니다."),
    PERFORMANCE_MEMBER_NOT_IN_PERFORMANCE(HttpStatus.BAD_REQUEST, "PM006", "공연 참여 인원이 공연에 속하지 않습니다."),

    // 400 Bad Request - Team / confirmed song
    PERFORMANCE_CONFIRMED_SONG_NOT_IN_PERFORMANCE(HttpStatus.BAD_REQUEST, "PCS002", "공연 확정곡이 공연에 속하지 않습니다."),
    PERFORMANCE_CONFIRMED_SONG_NOT_IN_TEAM_PERFORMANCE(HttpStatus.BAD_REQUEST, "PCS003", "공연 확정곡이 팀 공연에 속하지 않습니다."),
    PERFORMANCE_MEMBER_NOT_IN_TEAM_PERFORMANCE(HttpStatus.BAD_REQUEST, "PM007", "공연 참여 인원이 팀 공연에 속하지 않습니다."),
    TEAM_NOT_IN_PERFORMANCE(HttpStatus.BAD_REQUEST, "T003", "팀이 공연에 속하지 않습니다."),
    TEAM_NOT_IN_SONG_REQUEST_PERFORMANCE(HttpStatus.BAD_REQUEST, "T004", "팀이 희망곡 신청 공연에 속하지 않습니다."),

    // 400 Bad Request - Song
    SONG_REQUEST_NOT_IN_LINK_PERFORMANCE(HttpStatus.BAD_REQUEST, "SR002", "희망곡 신청이 링크 공연에 속하지 않습니다."),

    // 400 Bad Request - Song preference
    SONG_PREFERENCE_ALL_CONFIRMED_SONGS_REQUIRED(HttpStatus.BAD_REQUEST, "SP001", "모든 공연 확정곡에 대한 선호도를 제출해야 합니다."),
    SONG_PREFERENCE_ITEM_REQUIRED(HttpStatus.BAD_REQUEST, "SP002", "선호도 항목은 필수입니다."),
    PERFORMANCE_CONFIRMED_SONG_ID_REQUIRED(HttpStatus.BAD_REQUEST, "SP003", "공연 확정곡 ID는 필수입니다."),
    SONG_PREFERENCE_RANK_INVALID(HttpStatus.BAD_REQUEST, "SP004", "선호도 순위는 1 이상이어야 합니다."),
    DUPLICATE_SONG_PREFERENCE_CONFIRMED_SONG(HttpStatus.BAD_REQUEST, "SP005", "중복된 공연 확정곡 선호도입니다."),

    // 400 Bad Request - Setlist
    SETLIST_ITEM_REQUIRED(HttpStatus.BAD_REQUEST, "SL001", "셋리스트 항목은 필수입니다."),
    SETLIST_TEAM_ID_REQUIRED(HttpStatus.BAD_REQUEST, "SL002", "셋리스트 팀 ID는 필수입니다."),
    SETLIST_SEQUENCE_INVALID(HttpStatus.BAD_REQUEST, "SL003", "셋리스트 순서는 1 이상이어야 합니다."),
    DUPLICATE_SETLIST_TEAM(HttpStatus.BAD_REQUEST, "SL004", "셋리스트에 중복된 팀이 있습니다."),
    DUPLICATE_SETLIST_SEQUENCE(HttpStatus.BAD_REQUEST, "SL005", "셋리스트에 중복된 순서가 있습니다."),

    // 404 Not Found
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "C002", "요청한 리소스를 찾을 수 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "존재하지 않는 유저입니다."),
    USER_SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "US001", "존재하지 않는 유저 세션입니다."),
    TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "T001", "존재하지 않는 팀입니다."),
    PERFORMANCE_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "존재하지 않는 공연입니다."),
    PERFORMANCE_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "PM001", "존재하지 않는 공연 참여 인원입니다."),
    TEAM_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "TM001", "존재하지 않는 팀원입니다."),
    SONG_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "SR001", "존재하지 않는 희망곡 신청입니다."),
    SONG_VOTE_NOT_FOUND(HttpStatus.NOT_FOUND, "SV001", "존재하지 않는 희망곡 투표입니다."),
    PERFORMANCE_CONFIRMED_SONG_NOT_FOUND(HttpStatus.NOT_FOUND, "PCS001", "존재하지 않는 공연 확정곡입니다."),
    SONG_PREFERENCE_NOT_FOUND(HttpStatus.NOT_FOUND, "SP006", "존재하지 않는 선호도 응답입니다."),
    FINAL_SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "FS004", "존재하지 않는 최종 합주 일정입니다."),
    INPUT_LINK_NOT_FOUND(HttpStatus.NOT_FOUND, "L005", "존재하지 않는 입력 링크입니다."),
    PERFORMANCE_SETLIST_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "SL006", "존재하지 않는 셋리스트 항목입니다."),

    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S001", "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
