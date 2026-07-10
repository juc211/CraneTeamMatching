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
@Table(name = "song_preferences")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
/**
 * 공연 단위 확정곡에 대한 공연 참여 인원의 선호 순위
 */
public class SongPreference {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	private PerformanceConfirmedSong performanceConfirmedSong;

	@ManyToOne(fetch = FetchType.LAZY)
	private PerformanceMember performanceMember;

	private Integer rank;

	public static SongPreference create(
			PerformanceConfirmedSong performanceConfirmedSong,
			PerformanceMember performanceMember,
			Integer rank
	) {
		SongPreference songPreference = new SongPreference();
		songPreference.performanceConfirmedSong = performanceConfirmedSong;
		songPreference.performanceMember = performanceMember;
		songPreference.updateRank(rank);
		return songPreference;
	}

	public void updateRank(Integer rank) {
		if (rank == null || rank < 1) {
			throw new IllegalArgumentException("Song preference rank must be positive");
		}
		this.rank = rank;
	}
}
