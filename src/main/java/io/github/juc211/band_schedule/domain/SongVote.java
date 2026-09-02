package io.github.juc211.band_schedule.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
		name = "song_votes",
		uniqueConstraints = {
				@UniqueConstraint(
						name = "uk_song_votes_request_voter",
						columnNames = {
								"song_request_id",
								"voter_member_id"
						}
				)
		}
)
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
	@JoinColumn(name = "song_request_id", nullable = false)
	private SongRequest songRequest;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "voter_member_id", nullable = false)
	private PerformanceMember voterMember;

	//가능 불가능 보류
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private Vote vote;

	@Column(length = 500)
	private String reason;

	public static SongVote create(SongRequest songRequest, PerformanceMember voterMember, Vote vote, String reason) {
		SongVote songVote = new SongVote();
		songVote.songRequest = songRequest;
		songVote.voterMember = voterMember;
		songVote.vote = vote;
		songVote.reason = reason;
		return songVote;
	}

	public void update(Vote vote, String reason) {
		this.vote = vote;
		this.reason = reason;
	}
}
