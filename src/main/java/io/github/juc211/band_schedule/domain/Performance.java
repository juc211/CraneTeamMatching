package io.github.juc211.band_schedule.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 공연 (축제, 정기공연 등)
 */
@Entity
@Table(name = "performances")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Performance {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String title;

	private LocalDate performanceDate;

	private String location;

	private LocalDate scheduleWindowStartDate;

	private LocalDate scheduleWindowEndDate;

	private LocalDateTime createdAt;

	public static Performance create(String title, LocalDate performanceDate, String location) {
		return create(title, performanceDate, location, null, null);
	}

	public static Performance create(
			String title,
			LocalDate performanceDate,
			String location,
			LocalDate scheduleWindowStartDate,
			LocalDate scheduleWindowEndDate
	) {
		Performance performance = new Performance();
		performance.title = title;
		performance.performanceDate = performanceDate;
		performance.location = location;
		performance.updateScheduleWindowFields(scheduleWindowStartDate, scheduleWindowEndDate);
		performance.createdAt = LocalDateTime.now();
		return performance;
	}

	public void update(
			String title,
			LocalDate performanceDate,
			String location,
			LocalDate scheduleWindowStartDate,
			LocalDate scheduleWindowEndDate
	) {
		this.title = title;
		this.performanceDate = performanceDate;
		this.location = location;
		updateScheduleWindowFields(scheduleWindowStartDate, scheduleWindowEndDate);
	}

	private void updateScheduleWindowFields(LocalDate scheduleWindowStartDate, LocalDate scheduleWindowEndDate) {
		validateScheduleWindow(scheduleWindowStartDate, scheduleWindowEndDate);
		this.scheduleWindowStartDate = scheduleWindowStartDate;
		this.scheduleWindowEndDate = scheduleWindowEndDate;
	}

	private void validateScheduleWindow(LocalDate scheduleWindowStartDate, LocalDate scheduleWindowEndDate) {
		if (scheduleWindowStartDate == null && scheduleWindowEndDate == null) {
			return;
		}
		if (scheduleWindowStartDate == null || scheduleWindowEndDate == null) {
			throw new IllegalArgumentException("Schedule window start date and end date must be set together");
		}
		if (scheduleWindowStartDate.isAfter(scheduleWindowEndDate)) {
			throw new IllegalArgumentException("Schedule window start date must not be after end date");
		}
		if (scheduleWindowEndDate.isAfter(performanceDate)) {
			throw new IllegalArgumentException("Schedule window end date must not be after performance date");
		}
	}
}
