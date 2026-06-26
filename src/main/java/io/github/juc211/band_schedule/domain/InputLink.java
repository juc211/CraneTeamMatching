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
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "input_links")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InputLink {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String token;

	@ManyToOne(fetch = FetchType.LAZY)
	private Performance performance;

	@ManyToOne(fetch = FetchType.LAZY)
	private Team team;

	@Enumerated(EnumType.STRING)
	private InputLinkType type;

	private boolean active;

	private LocalDateTime expiresAt;

	private LocalDateTime createdAt;
}
