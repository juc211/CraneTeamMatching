package io.github.juc211.band_schedule.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "final_schedules")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
/**
 * 최종 합주 날짜
 */
public class FinalSchedule {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "team_id", nullable = false)
	private Team team;

	@Column(nullable = false)
	private LocalDateTime startDateTime;

	@Column(nullable = false)
	private LocalDateTime endDateTime;

	@Column(length = 1000)
	private String memo;

	public static FinalSchedule create(Team team, LocalDateTime startDateTime, LocalDateTime endDateTime, String memo) {
		FinalSchedule finalSchedule = new FinalSchedule();
		finalSchedule.team = team;
		finalSchedule.update(startDateTime, endDateTime, memo);
		return finalSchedule;
	}

	public void update(LocalDateTime startDateTime, LocalDateTime endDateTime, String memo) {
		validateTimeRange(startDateTime, endDateTime);
		this.startDateTime = startDateTime;
		this.endDateTime = endDateTime;
		this.memo = memo;
	}

	private void validateTimeRange(LocalDateTime startDateTime, LocalDateTime endDateTime) {
		if (startDateTime == null || endDateTime == null) {
			throw new IllegalArgumentException("Final schedule start and end date time must be set together");
		}
		if (!startDateTime.isBefore(endDateTime)) {
			throw new IllegalArgumentException("Final schedule start date time must be before end date time");
		}
	}
}
