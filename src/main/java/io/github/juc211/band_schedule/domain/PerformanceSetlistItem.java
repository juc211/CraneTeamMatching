package io.github.juc211.band_schedule.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
		name = "performance_setlist_items",
		uniqueConstraints = {
				@UniqueConstraint(
						name = "uk_setlist_items_performance_team",
						columnNames = {
								"performance_id",
								"team_id"
						}
				),
				@UniqueConstraint(
						name = "uk_setlist_items_performance_sequence",
						columnNames = {
								"performance_id",
								"sequence_number"
						}
				)
		}
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
/**
 * 공연에서 진행할 팀별 확정곡 순서
 */
public class PerformanceSetlistItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "performance_id", nullable = false)
	private Performance performance;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "team_id", nullable = false)
	private Team team;

	@Column(nullable = false)
	private Integer sequenceNumber;

	public static PerformanceSetlistItem create(Performance performance, Team team, Integer sequenceNumber) {
		PerformanceSetlistItem performanceSetlistItem = new PerformanceSetlistItem();
		performanceSetlistItem.performance = performance;
		performanceSetlistItem.team = team;
		performanceSetlistItem.updateSequenceNumber(sequenceNumber);
		return performanceSetlistItem;
	}

	public void updateSequenceNumber(Integer sequenceNumber) {
		if (sequenceNumber == null || sequenceNumber < 1) {
			throw new IllegalArgumentException("Setlist sequence number must be positive");
		}
		this.sequenceNumber = sequenceNumber;
	}
}
