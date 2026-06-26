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
public class SongVote {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	private SongRequest songRequest;

	@ManyToOne(fetch = FetchType.LAZY)
	private PerformanceMember voterMember;

	@Enumerated(EnumType.STRING)
	private Vote vote;

	private String reason;
}
