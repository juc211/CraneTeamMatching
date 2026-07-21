package io.github.juc211.band_schedule.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.juc211.band_schedule.domain.Performance;
import io.github.juc211.band_schedule.domain.PerformanceSetlistItem;
import io.github.juc211.band_schedule.domain.Team;
import io.github.juc211.band_schedule.dto.PerformanceSetlistDto;
import io.github.juc211.band_schedule.exception.BusinessException;
import io.github.juc211.band_schedule.repository.PerformanceRepository;
import io.github.juc211.band_schedule.repository.PerformanceSetlistItemRepository;
import io.github.juc211.band_schedule.repository.TeamRepository;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class PerformanceSetlistServiceTest {

	@Autowired
	private PerformanceSetlistService performanceSetlistService;

	@Autowired
	private PerformanceSetlistItemRepository performanceSetlistItemRepository;

	@Autowired
	private PerformanceRepository performanceRepository;

	@Autowired
	private TeamRepository teamRepository;

	@Test
	void replaceSetlistPersistsItemsInSequenceOrder() {
		Performance performance = createPerformance();
		Team firstTeam = teamRepository.save(Team.create(performance, "Team A", "Song A"));
		Team secondTeam = teamRepository.save(Team.create(performance, "Team B", "Song B"));

		List<PerformanceSetlistDto.PerformanceSetlistItemResponse> responses = performanceSetlistService.replaceSetlist(
				performance.getId(),
				new PerformanceSetlistDto.PerformanceSetlistReplaceRequest(List.of(
						new PerformanceSetlistDto.PerformanceSetlistItemRequest(secondTeam.getId(), 1),
						new PerformanceSetlistDto.PerformanceSetlistItemRequest(firstTeam.getId(), 2)
				))
		);

		assertThat(responses)
				.extracting(PerformanceSetlistDto.PerformanceSetlistItemResponse::teamName)
				.containsExactly("Team B", "Team A");
		assertThat(responses)
				.extracting(PerformanceSetlistDto.PerformanceSetlistItemResponse::confirmedSong)
				.containsExactly("Song B", "Song A");
		assertThat(performanceSetlistItemRepository.findAll()).hasSize(2);
	}

	@Test
	void replaceSetlistDeletesPreviousItems() {
		Performance performance = createPerformance();
		Team firstTeam = teamRepository.save(Team.create(performance, "Team A", "Song A"));
		Team secondTeam = teamRepository.save(Team.create(performance, "Team B", "Song B"));
		performanceSetlistItemRepository.save(PerformanceSetlistItem.create(performance, firstTeam, 1));

		performanceSetlistService.replaceSetlist(
				performance.getId(),
				new PerformanceSetlistDto.PerformanceSetlistReplaceRequest(List.of(
						new PerformanceSetlistDto.PerformanceSetlistItemRequest(secondTeam.getId(), 1)
				))
		);

		assertThat(performanceSetlistService.getSetlist(performance.getId()))
				.extracting(PerformanceSetlistDto.PerformanceSetlistItemResponse::teamName)
				.containsExactly("Team B");
	}

	@Test
	void replaceSetlistRejectsTeamFromDifferentPerformance() {
		Performance performance = createPerformance();
		Performance otherPerformance = createPerformance();
		Team otherTeam = teamRepository.save(Team.create(otherPerformance, "Other Team", "Other Song"));

		assertThatThrownBy(() -> performanceSetlistService.replaceSetlist(
				performance.getId(),
				new PerformanceSetlistDto.PerformanceSetlistReplaceRequest(List.of(
						new PerformanceSetlistDto.PerformanceSetlistItemRequest(otherTeam.getId(), 1)
				))
		))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("Team does not belong to performance");
	}

	@Test
	void replaceSetlistRejectsDuplicateSequenceNumber() {
		Performance performance = createPerformance();
		Team firstTeam = teamRepository.save(Team.create(performance, "Team A", "Song A"));
		Team secondTeam = teamRepository.save(Team.create(performance, "Team B", "Song B"));

		assertThatThrownBy(() -> performanceSetlistService.replaceSetlist(
				performance.getId(),
				new PerformanceSetlistDto.PerformanceSetlistReplaceRequest(List.of(
						new PerformanceSetlistDto.PerformanceSetlistItemRequest(firstTeam.getId(), 1),
						new PerformanceSetlistDto.PerformanceSetlistItemRequest(secondTeam.getId(), 1)
				))
		))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("Setlist cannot contain duplicate sequence number");
	}

	@Test
	void replaceSetlistRejectsNullItem() {
		Performance performance = createPerformance();

		assertThatThrownBy(() -> performanceSetlistService.replaceSetlist(
				performance.getId(),
				new PerformanceSetlistDto.PerformanceSetlistReplaceRequest(Collections.singletonList(null))
		))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("Setlist item is required");
	}

	@Test
	void replaceSetlistRejectsDuplicateTeam() {
		Performance performance = createPerformance();
		Team team = teamRepository.save(Team.create(performance, "Team A", "Song A"));

		assertThatThrownBy(() -> performanceSetlistService.replaceSetlist(
				performance.getId(),
				new PerformanceSetlistDto.PerformanceSetlistReplaceRequest(List.of(
						new PerformanceSetlistDto.PerformanceSetlistItemRequest(team.getId(), 1),
						new PerformanceSetlistDto.PerformanceSetlistItemRequest(team.getId(), 2)
				))
		))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("Setlist cannot contain duplicate team");
	}

	@Test
	void deleteSetlistRemovesAllItemsInPerformance() {
		Performance performance = createPerformance();
		Team firstTeam = teamRepository.save(Team.create(performance, "Team A", "Song A"));
		Team secondTeam = teamRepository.save(Team.create(performance, "Team B", "Song B"));
		performanceSetlistItemRepository.save(PerformanceSetlistItem.create(performance, firstTeam, 1));
		performanceSetlistItemRepository.save(PerformanceSetlistItem.create(performance, secondTeam, 2));

		performanceSetlistService.deleteSetlist(performance.getId());

		assertThat(performanceSetlistService.getSetlist(performance.getId())).isEmpty();
	}

	private Performance createPerformance() {
		return performanceRepository.save(Performance.create(
				"2026 Summer Concert",
				LocalDate.of(2026, 8, 20),
				"Main Hall"
		));
	}
}
