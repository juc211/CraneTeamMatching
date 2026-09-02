package io.github.juc211.band_schedule.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
		name = "performance_members",
		uniqueConstraints = {
				@UniqueConstraint(
						name = "uk_performance_members_performance_user",
						columnNames = {
								"performance_id",
								"user_id"
						}
				)
		}
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PerformanceMember {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "performance_id", nullable = false)
	private Performance performance;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	public static PerformanceMember create(Performance performance, User user) {
		PerformanceMember performanceMember = new PerformanceMember();
		performanceMember.performance = performance;
		performanceMember.user = user;
		return performanceMember;
	}
}
