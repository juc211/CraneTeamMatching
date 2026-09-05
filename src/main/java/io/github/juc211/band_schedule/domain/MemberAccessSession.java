package io.github.juc211.band_schedule.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 입력 링크에서 식별된 공연 참여 인원의 임시 접근 세션
 */
@Entity
@Table(name = "member_access_sessions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberAccessSession {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 128, unique = true)
	private String token;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "performance_member_id", nullable = false)
	private PerformanceMember performanceMember;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "input_link_id", nullable = false)
	private InputLink inputLink;

	@Column(nullable = false)
	private LocalDateTime expiresAt;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	public static MemberAccessSession create(
			String token,
			PerformanceMember performanceMember,
			InputLink inputLink,
			LocalDateTime expiresAt
	) {
		MemberAccessSession session = new MemberAccessSession();
		session.token = token;
		session.performanceMember = performanceMember;
		session.inputLink = inputLink;
		session.expiresAt = expiresAt;
		session.createdAt = LocalDateTime.now();
		return session;
	}

	public boolean isExpired() {
		return expiresAt.isBefore(LocalDateTime.now());
	}
}
