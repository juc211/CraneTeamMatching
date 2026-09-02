package io.github.juc211.band_schedule.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
		name = "team_members",
		uniqueConstraints = {
				@UniqueConstraint(
						name = "uk_team_members_team_performance_member",
						columnNames = {
								"team_id",
								"performance_member_id"
						}
				)
		}
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamMember {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "team_id", nullable = false)
	private Team team;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "performance_member_id", nullable = false)
	private PerformanceMember performanceMember;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private Part part;

	public static TeamMember create(Team team, PerformanceMember performanceMember, Part part) {
		TeamMember teamMember = new TeamMember();
		teamMember.team = team;
		teamMember.performanceMember = performanceMember;
		teamMember.part = part;
		return teamMember;
	}

	public void updatePart(Part part) {
		this.part = part;
	}
}
