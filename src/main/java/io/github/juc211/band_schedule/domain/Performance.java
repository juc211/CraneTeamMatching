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

	private LocalDateTime createdAt;

	public static Performance create(String title, LocalDate performanceDate, String location) {
		Performance performance = new Performance();
		performance.title = title;
		performance.performanceDate = performanceDate;
		performance.location = location;
		performance.createdAt = LocalDateTime.now();
		return performance;
	}

	public void update(String title, LocalDate performanceDate, String location) {
		this.title = title;
		this.performanceDate = performanceDate;
		this.location = location;
	}
}
