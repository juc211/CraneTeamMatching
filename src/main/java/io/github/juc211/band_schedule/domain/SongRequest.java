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
@Table(name = "song_requests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
/**
 * 곡 신청
 */
public class SongRequest {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	private Performance performance;

	@ManyToOne(fetch = FetchType.LAZY)
	private Team team;

	@ManyToOne(fetch = FetchType.LAZY)
	private PerformanceMember requestedByMember;

	//song ([곡 명 - 가수] 형식)
	private String song;

	private LocalDateTime createdAt;

	public static SongRequest create(Performance performance, Team team, PerformanceMember requestedByMember, String song) {
		SongRequest songRequest = new SongRequest();
		songRequest.performance = performance;
		songRequest.team = team;
		songRequest.requestedByMember = requestedByMember;
		songRequest.song = song;
		songRequest.createdAt = LocalDateTime.now();
		return songRequest;
	}

	public void update(Team team, String song) {
		this.team = team;
		this.song = song;
	}
}
