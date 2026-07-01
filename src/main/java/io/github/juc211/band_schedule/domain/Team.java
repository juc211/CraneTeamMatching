package io.github.juc211.band_schedule.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
	private Performance performance;

	private String name;

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
}
