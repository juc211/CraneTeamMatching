package io.github.juc211.band_schedule.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "input_links")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
/**
 * 멤버들에게 입력을 받기위한 링크 생성 담당
 */
public class InputLink {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 64, unique = true)
	private String token;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "performance_id", nullable = false)
	private Performance performance;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private InputLinkType type;

	@Column(nullable = false)
	private boolean active;

	private LocalDateTime expiresAt;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	public static InputLink create(String token, Performance performance, boolean active, LocalDateTime expiresAt) {
		return create(token, performance, InputLinkType.SONG_REQUEST, active, expiresAt);
	}

	public static InputLink create(String token, Performance performance, InputLinkType type, boolean active, LocalDateTime expiresAt) {
		InputLink inputLink = new InputLink();
		inputLink.token = token;
		inputLink.performance = performance;
		inputLink.type = type;
		inputLink.active = active;
		inputLink.expiresAt = expiresAt;
		inputLink.createdAt = LocalDateTime.now();
		return inputLink;
	}

	public void updateActive(boolean active) {
		this.active = active;
	}

	public void updateExpiresAt(LocalDateTime expiresAt) {
		this.expiresAt = expiresAt;
	}
}
