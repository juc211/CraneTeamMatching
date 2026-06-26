package io.github.juc211.band_schedule.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "team_members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamMember {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	private Team team;

	@ManyToOne(fetch = FetchType.LAZY)
	private PerformanceMember performanceMember;

	@Enumerated(EnumType.STRING)
	private Part part;
}
