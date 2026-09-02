package io.github.juc211.band_schedule.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "clubs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Club {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(nullable = false, length = 36, unique = true)
	private String adminToken;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	public static Club create(String name) {
		Club club = new Club();
		club.name = name;
		club.adminToken = UUID.randomUUID().toString();
		club.createdAt = LocalDateTime.now();
		return club;
	}

	public void reissueAdminToken() {
		this.adminToken = UUID.randomUUID().toString();
	}
}
