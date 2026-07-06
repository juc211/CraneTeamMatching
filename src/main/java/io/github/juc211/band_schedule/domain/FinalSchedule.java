package io.github.juc211.band_schedule.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
	private Team team;

	private LocalDateTime startDateTime;

	private LocalDateTime endDateTime;

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
