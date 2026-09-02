package io.github.juc211.band_schedule.domain;

import jakarta.persistence.*;

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
	@JoinColumn(name = "performance_id", nullable = false)
	private Performance performance;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "team_id")
	private Team team;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "requested_by_member_id", nullable = false)
	private PerformanceMember requestedByMember;

	//song ([곡 명 - 가수] 형식)
	@Column(nullable = false, length = 200)
	private String song;

	@Column(length = 500)
	private String youtubeUrl;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	public static SongRequest create(Performance performance, Team team, PerformanceMember requestedByMember, String song) {
		return create(performance, team, requestedByMember, song, null);
	}

	public static SongRequest create(Performance performance, Team team, PerformanceMember requestedByMember, String song, String youtubeUrl) {
		SongRequest songRequest = new SongRequest();
		songRequest.performance = performance;
		songRequest.team = team;
		songRequest.requestedByMember = requestedByMember;
		songRequest.song = song;
		songRequest.youtubeUrl = youtubeUrl;
		songRequest.createdAt = LocalDateTime.now();
		return songRequest;
	}

	public void update(Team team, String song) {
		update(team, song, youtubeUrl);
	}

	public void update(Team team, String song, String youtubeUrl) {
		this.team = team;
		this.song = song;
		this.youtubeUrl = youtubeUrl;
	}
}
