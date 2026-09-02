package io.github.juc211.band_schedule.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "performance_confirmed_songs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PerformanceConfirmedSong {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "performance_id", nullable = false)
	private Performance performance;

	@Column(nullable = false, length = 200)
	private String song;

	@Column(length = 1000)
	private String adminMemo;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	public static PerformanceConfirmedSong create(Performance performance, String song) {
		return create(performance, song, null);
	}

	public static PerformanceConfirmedSong create(Performance performance, String song, String adminMemo) {
		PerformanceConfirmedSong performanceConfirmedSong = new PerformanceConfirmedSong();
		performanceConfirmedSong.performance = performance;
		performanceConfirmedSong.song = song;
		performanceConfirmedSong.adminMemo = adminMemo;
		performanceConfirmedSong.createdAt = LocalDateTime.now();
		return performanceConfirmedSong;
	}

	public void update(String song) {
		update(song, adminMemo);
	}

	public void update(String song, String adminMemo) {
		this.song = song;
		this.adminMemo = adminMemo;
	}
}
