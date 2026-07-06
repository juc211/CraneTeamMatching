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
@Table(name = "available_time")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
/**
 * 팀 내부에서 팀원 개인 가능 시간 종합
 */
public class AvailableTime {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	private TeamMember teamMember;

	private LocalDateTime startDateTime;

	private LocalDateTime endDateTime;

	public static AvailableTime create(TeamMember teamMember, LocalDateTime startDateTime, LocalDateTime endDateTime) {
		AvailableTime availableTime = new AvailableTime();
		availableTime.teamMember = teamMember;
		availableTime.update(startDateTime, endDateTime);
		return availableTime;
	}

	public void update(LocalDateTime startDateTime, LocalDateTime endDateTime) {
		validateTimeRange(startDateTime, endDateTime);
		this.startDateTime = startDateTime;
		this.endDateTime = endDateTime;
	}

	private void validateTimeRange(LocalDateTime startDateTime, LocalDateTime endDateTime) {
		if (startDateTime == null || endDateTime == null) {
			throw new IllegalArgumentException("Available time start and end date time must be set together");
		}
		if (!startDateTime.isBefore(endDateTime)) {
			throw new IllegalArgumentException("Available time start date time must be before end date time");
		}
	}
}
