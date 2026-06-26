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

	private String song;

	private LocalDateTime createdAt;
}
