package io.github.juc211.band_schedule.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
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
@Table(name = "song_votes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
/**
 * 신청된 곡의 가능 여부를 판단.
 */
public class SongVote {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	private SongRequest songRequest;

	@ManyToOne(fetch = FetchType.LAZY)
	private PerformanceMember voterMember;

	//가능 불가능 보류
	@Enumerated(EnumType.STRING)
	private Vote vote;

	private String reason;
}
