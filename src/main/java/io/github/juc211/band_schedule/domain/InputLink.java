package io.github.juc211.band_schedule.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

	private String token;

	@ManyToOne(fetch = FetchType.LAZY)
	private Performance performance;

	@Enumerated(EnumType.STRING)
	private InputLinkType type;

	private boolean active;

	private LocalDateTime expiresAt;

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
