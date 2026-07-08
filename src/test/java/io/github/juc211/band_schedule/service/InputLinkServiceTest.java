package io.github.juc211.band_schedule.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.juc211.band_schedule.domain.InputLink;
import io.github.juc211.band_schedule.domain.InputLinkType;
import io.github.juc211.band_schedule.domain.Performance;
import io.github.juc211.band_schedule.dto.InputLinkDto;
import io.github.juc211.band_schedule.repository.InputLinkRepository;
import io.github.juc211.band_schedule.repository.PerformanceRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class InputLinkServiceTest {

	@Autowired
	private InputLinkService inputLinkService;

	@Autowired
	private InputLinkRepository inputLinkRepository;

	@Autowired
	private PerformanceRepository performanceRepository;

	@Test
	void createInputLinkPersistsTypedLink() {
		Performance performance = createPerformance();

		InputLinkDto.InputLinkResponse response = inputLinkService.createInputLink(
				performance.getId(),
				new InputLinkDto.InputLinkCreateRequest(InputLinkType.AVAILABLE_TIME, LocalDateTime.of(2026, 8, 1, 23, 59))
		);

		InputLink savedInputLink = inputLinkRepository.findById(response.inputLinkId()).orElseThrow();
		assertThat(response.performanceId()).isEqualTo(performance.getId());
		assertThat(response.type()).isEqualTo(InputLinkType.AVAILABLE_TIME);
		assertThat(response.token()).isNotBlank();
		assertThat(response.active()).isTrue();
		assertThat(savedInputLink.getType()).isEqualTo(InputLinkType.AVAILABLE_TIME);
	}

	@Test
	void getInputLinksByPerformanceReturnsLinksInPerformance() {
		Performance performance = createPerformance();
		inputLinkRepository.save(InputLink.create("available-token", performance, InputLinkType.AVAILABLE_TIME, true, null));
		inputLinkRepository.save(InputLink.create("view-token", performance, InputLinkType.FINAL_SCHEDULE_VIEW, true, null));

		assertThat(inputLinkService.getInputLinksByPerformance(performance.getId()))
				.extracting(InputLinkDto.InputLinkResponse::type)
				.containsExactly(InputLinkType.AVAILABLE_TIME, InputLinkType.FINAL_SCHEDULE_VIEW);
	}

	@Test
	void updateInputLinkActiveChangesActiveState() {
		Performance performance = createPerformance();
		InputLink inputLink = inputLinkRepository.save(InputLink.create("available-token", performance, InputLinkType.AVAILABLE_TIME, true, null));

		InputLinkDto.InputLinkResponse response = inputLinkService.updateInputLinkActive(
				inputLink.getId(),
				new InputLinkDto.InputLinkActiveUpdateRequest(false)
		);

		assertThat(response.active()).isFalse();
		assertThat(inputLinkRepository.findById(inputLink.getId()).orElseThrow().isActive()).isFalse();
	}

	@Test
	void updateInputLinkExpiresAtChangesExpiresAt() {
		Performance performance = createPerformance();
		InputLink inputLink = inputLinkRepository.save(InputLink.create(
				"available-token",
				performance,
				InputLinkType.AVAILABLE_TIME,
				true,
				LocalDateTime.of(2026, 8, 1, 23, 59)
		));

		InputLinkDto.InputLinkResponse response = inputLinkService.updateInputLinkExpiresAt(
				inputLink.getId(),
				new InputLinkDto.InputLinkExpiresAtUpdateRequest(LocalDateTime.of(2026, 8, 5, 23, 59))
		);

		assertThat(response.expiresAt()).isEqualTo(LocalDateTime.of(2026, 8, 5, 23, 59));
		assertThat(inputLinkRepository.findById(inputLink.getId()).orElseThrow().getExpiresAt())
				.isEqualTo(LocalDateTime.of(2026, 8, 5, 23, 59));
	}

	@Test
	void updateInputLinkExpiresAtCanClearExpiresAt() {
		Performance performance = createPerformance();
		InputLink inputLink = inputLinkRepository.save(InputLink.create(
				"available-token",
				performance,
				InputLinkType.AVAILABLE_TIME,
				true,
				LocalDateTime.of(2026, 8, 1, 23, 59)
		));

		InputLinkDto.InputLinkResponse response = inputLinkService.updateInputLinkExpiresAt(
				inputLink.getId(),
				new InputLinkDto.InputLinkExpiresAtUpdateRequest(null)
		);

		assertThat(response.expiresAt()).isNull();
		assertThat(inputLinkRepository.findById(inputLink.getId()).orElseThrow().getExpiresAt()).isNull();
	}

	@Test
	void deleteInputLinkRemovesLink() {
		Performance performance = createPerformance();
		InputLink inputLink = inputLinkRepository.save(InputLink.create("available-token", performance, InputLinkType.AVAILABLE_TIME, true, null));

		inputLinkService.deleteInputLink(inputLink.getId());

		assertThat(inputLinkRepository.findById(inputLink.getId())).isEmpty();
	}

	private Performance createPerformance() {
		return performanceRepository.save(Performance.create(
				"2026 Summer Concert",
				LocalDate.of(2026, 8, 20),
				"Main Hall"
		));
	}
}
