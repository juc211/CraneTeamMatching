package io.github.juc211.band_schedule.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "teams")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
/**
 * 한 공연 안에서 나눠질 팀(1곡 = 1팀으로 구성)
 */
public class Team {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "performance_id", nullable = false)
	private Performance performance;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(length = 200)
	private String confirmedSong;

	public static Team create(Performance performance, String name, String confirmedSong) {
		Team team = new Team();
		team.performance = performance;
		team.name = name;
		team.confirmedSong = confirmedSong;
		return team;
	}

	public void update(String name, String confirmedSong) {
		this.name = name;
		this.confirmedSong = confirmedSong;
	}

	public void updateConfirmedSong(String confirmedSong) {
		this.confirmedSong = confirmedSong;
	}
}
