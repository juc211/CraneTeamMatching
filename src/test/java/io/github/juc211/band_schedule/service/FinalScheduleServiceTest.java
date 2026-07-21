package io.github.juc211.band_schedule.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.juc211.band_schedule.domain.FinalSchedule;
import io.github.juc211.band_schedule.domain.InputLink;
import io.github.juc211.band_schedule.domain.InputLinkType;
import io.github.juc211.band_schedule.domain.Performance;
import io.github.juc211.band_schedule.domain.Team;
import io.github.juc211.band_schedule.dto.FinalScheduleDto;
import io.github.juc211.band_schedule.exception.BusinessException;
import io.github.juc211.band_schedule.repository.FinalScheduleRepository;
import io.github.juc211.band_schedule.repository.InputLinkRepository;
import io.github.juc211.band_schedule.repository.PerformanceRepository;
import io.github.juc211.band_schedule.repository.TeamRepository;
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
class FinalScheduleServiceTest {

	@Autowired
	private FinalScheduleService finalScheduleService;

	@Autowired
	private FinalScheduleRepository finalScheduleRepository;

	@Autowired
	private PerformanceRepository performanceRepository;

	@Autowired
	private TeamRepository teamRepository;

	@Autowired
	private InputLinkRepository inputLinkRepository;

	@Test
	void createFinalSchedulePersistsScheduleOutsideScheduleWindow() {
		Team team = createTeamWithScheduleWindow();

		FinalScheduleDto.FinalScheduleResponse response = finalScheduleService.createFinalSchedule(
				team.getId(),
				new FinalScheduleDto.FinalScheduleCreateRequest(
						LocalDateTime.of(2026, 8, 21, 18, 0),
						LocalDateTime.of(2026, 8, 21, 20, 0),
						"관리자 임의 추가"
				)
		);

		FinalSchedule savedFinalSchedule = finalScheduleRepository.findById(response.finalScheduleId()).orElseThrow();
		assertThat(response.teamId()).isEqualTo(team.getId());
		assertThat(response.performanceId()).isEqualTo(team.getPerformance().getId());
		assertThat(response.teamName()).isEqualTo("Team A");
		assertThat(response.confirmedSong()).isEqualTo("Song A");
		assertThat(savedFinalSchedule.getStartDateTime()).isEqualTo(LocalDateTime.of(2026, 8, 21, 18, 0));
		assertThat(savedFinalSchedule.getEndDateTime()).isEqualTo(LocalDateTime.of(2026, 8, 21, 20, 0));
	}

	@Test
	void createFinalScheduleAllowsAdjacentScheduleInSamePerformance() {
		Performance performance = createPerformance();
		Team firstTeam = teamRepository.save(Team.create(performance, "Team A", "Song A"));
		Team secondTeam = teamRepository.save(Team.create(performance, "Team B", "Song B"));
		finalScheduleRepository.save(FinalSchedule.create(
				firstTeam,
				LocalDateTime.of(2026, 8, 1, 18, 0),
				LocalDateTime.of(2026, 8, 1, 19, 0),
				null
		));

		FinalScheduleDto.FinalScheduleResponse response = finalScheduleService.createFinalSchedule(
				secondTeam.getId(),
				new FinalScheduleDto.FinalScheduleCreateRequest(
						LocalDateTime.of(2026, 8, 1, 19, 0),
						LocalDateTime.of(2026, 8, 1, 20, 0),
						null
				)
		);

		assertThat(response.startDateTime()).isEqualTo(LocalDateTime.of(2026, 8, 1, 19, 0));
		assertThat(finalScheduleRepository.findAll()).hasSize(2);
	}

	@Test
	void createFinalScheduleRejectsOverlappingScheduleInSamePerformance() {
		Performance performance = createPerformance();
		Team firstTeam = teamRepository.save(Team.create(performance, "Team A", "Song A"));
		Team secondTeam = teamRepository.save(Team.create(performance, "Team B", "Song B"));
		finalScheduleRepository.save(FinalSchedule.create(
				firstTeam,
				LocalDateTime.of(2026, 8, 1, 18, 0),
				LocalDateTime.of(2026, 8, 1, 19, 0),
				null
		));

		assertThatThrownBy(() -> finalScheduleService.createFinalSchedule(
				secondTeam.getId(),
				new FinalScheduleDto.FinalScheduleCreateRequest(
						LocalDateTime.of(2026, 8, 1, 18, 30),
						LocalDateTime.of(2026, 8, 1, 19, 30),
						null
				)
		))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("Final schedule overlaps with another final schedule");
	}

	@Test
	void createFinalScheduleRejectsOverlappingScheduleInDifferentPerformance() {
		Team firstTeam = teamRepository.save(Team.create(createPerformance(), "Team A", "Song A"));
		Team secondTeam = teamRepository.save(Team.create(createPerformance(), "Team B", "Song B"));
		finalScheduleRepository.save(FinalSchedule.create(
				firstTeam,
				LocalDateTime.of(2026, 8, 1, 18, 0),
				LocalDateTime.of(2026, 8, 1, 19, 0),
				null
		));

		assertThatThrownBy(() -> finalScheduleService.createFinalSchedule(
				secondTeam.getId(),
				new FinalScheduleDto.FinalScheduleCreateRequest(
						LocalDateTime.of(2026, 8, 1, 18, 0),
						LocalDateTime.of(2026, 8, 1, 19, 0),
						null
				)
		))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("Final schedule overlaps with another final schedule");
	}

	@Test
	void getFinalSchedulesByTeamReturnsSchedulesInTeam() {
		Team team = createTeamWithScheduleWindow();
		finalScheduleRepository.save(FinalSchedule.create(
				team,
				LocalDateTime.of(2026, 8, 7, 22, 0),
				LocalDateTime.of(2026, 8, 7, 23, 0),
				null
		));
		finalScheduleRepository.save(FinalSchedule.create(
				team,
				LocalDateTime.of(2026, 8, 1, 15, 0),
				LocalDateTime.of(2026, 8, 1, 17, 0),
				null
		));

		assertThat(finalScheduleService.getFinalSchedulesByTeam(team.getId()))
				.extracting(FinalScheduleDto.FinalScheduleResponse::startDateTime)
				.containsExactly(
						LocalDateTime.of(2026, 8, 1, 15, 0),
						LocalDateTime.of(2026, 8, 7, 22, 0)
				);
	}

	@Test
	void getFinalSchedulesByPerformanceReturnsSchedulesInPerformance() {
		Performance performance = createPerformance();
		Team firstTeam = teamRepository.save(Team.create(performance, "Team A", "Song A"));
		Team secondTeam = teamRepository.save(Team.create(performance, "Team B", "Song B"));
		finalScheduleRepository.save(FinalSchedule.create(
				firstTeam,
				LocalDateTime.of(2026, 8, 1, 15, 0),
				LocalDateTime.of(2026, 8, 1, 17, 0),
				null
		));
		finalScheduleRepository.save(FinalSchedule.create(
				secondTeam,
				LocalDateTime.of(2026, 8, 7, 22, 0),
				LocalDateTime.of(2026, 8, 7, 23, 0),
				null
		));

		assertThat(finalScheduleService.getFinalSchedulesByPerformance(performance.getId()))
				.extracting(FinalScheduleDto.FinalScheduleResponse::teamName)
				.containsExactly("Team A", "Team B");
	}

	@Test
	void getFinalSchedulesByLinkReturnsSchedulesInLinkPerformance() {
		Performance performance = createPerformance();
		Performance otherPerformance = createPerformance();
		inputLinkRepository.save(InputLink.create("view-token", performance, InputLinkType.FINAL_SCHEDULE_VIEW, true, null));
		Team firstTeam = teamRepository.save(Team.create(performance, "Team A", "Song A"));
		Team secondTeam = teamRepository.save(Team.create(performance, "Team B", "Song B"));
		Team otherTeam = teamRepository.save(Team.create(otherPerformance, "Other Team", "Other Song"));
		finalScheduleRepository.save(FinalSchedule.create(
				firstTeam,
				LocalDateTime.of(2026, 8, 1, 15, 0),
				LocalDateTime.of(2026, 8, 1, 17, 0),
				null
		));
		finalScheduleRepository.save(FinalSchedule.create(
				secondTeam,
				LocalDateTime.of(2026, 8, 7, 22, 0),
				LocalDateTime.of(2026, 8, 7, 23, 0),
				null
		));
		finalScheduleRepository.save(FinalSchedule.create(
				otherTeam,
				LocalDateTime.of(2026, 8, 8, 22, 0),
				LocalDateTime.of(2026, 8, 8, 23, 0),
				null
		));

		assertThat(finalScheduleService.getFinalSchedulesByLink("view-token"))
				.extracting(FinalScheduleDto.FinalScheduleResponse::teamName)
				.containsExactly("Team A", "Team B");
	}

	@Test
	void getFinalSchedulesByLinkAndTeamReturnsSchedulesInTeam() {
		Performance performance = createPerformance();
		inputLinkRepository.save(InputLink.create("view-token", performance, InputLinkType.FINAL_SCHEDULE_VIEW, true, null));
		Team firstTeam = teamRepository.save(Team.create(performance, "Team A", "Song A"));
		Team secondTeam = teamRepository.save(Team.create(performance, "Team B", "Song B"));
		finalScheduleRepository.save(FinalSchedule.create(
				firstTeam,
				LocalDateTime.of(2026, 8, 1, 15, 0),
				LocalDateTime.of(2026, 8, 1, 17, 0),
				null
		));
		finalScheduleRepository.save(FinalSchedule.create(
				secondTeam,
				LocalDateTime.of(2026, 8, 7, 22, 0),
				LocalDateTime.of(2026, 8, 7, 23, 0),
				null
		));

		assertThat(finalScheduleService.getFinalSchedulesByLinkAndTeam("view-token", secondTeam.getId()))
				.extracting(FinalScheduleDto.FinalScheduleResponse::teamName)
				.containsExactly("Team B");
	}

	@Test
	void getFinalSchedulesByLinkRejectsWrongLinkType() {
		Performance performance = createPerformance();
		inputLinkRepository.save(InputLink.create("available-token", performance, InputLinkType.AVAILABLE_TIME, true, null));

		assertThatThrownBy(() -> finalScheduleService.getFinalSchedulesByLink("available-token"))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("InputLink type must be FINAL_SCHEDULE_VIEW");
	}

	@Test
	void getFinalSchedulesByLinkAndTeamRejectsTeamFromDifferentPerformance() {
		Performance performance = createPerformance();
		Performance otherPerformance = createPerformance();
		inputLinkRepository.save(InputLink.create("view-token", performance, InputLinkType.FINAL_SCHEDULE_VIEW, true, null));
		Team otherTeam = teamRepository.save(Team.create(otherPerformance, "Other Team", "Other Song"));

		assertThatThrownBy(() -> finalScheduleService.getFinalSchedulesByLinkAndTeam("view-token", otherTeam.getId()))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("Team does not belong to link performance");
	}

	@Test
	void updateFinalScheduleChangesScheduleAndRejectsOverlap() {
		Performance performance = createPerformance();
		Team firstTeam = teamRepository.save(Team.create(performance, "Team A", "Song A"));
		Team secondTeam = teamRepository.save(Team.create(performance, "Team B", "Song B"));
		FinalSchedule target = finalScheduleRepository.save(FinalSchedule.create(
				firstTeam,
				LocalDateTime.of(2026, 8, 1, 18, 0),
				LocalDateTime.of(2026, 8, 1, 19, 0),
				null
		));
		finalScheduleRepository.save(FinalSchedule.create(
				secondTeam,
				LocalDateTime.of(2026, 8, 1, 20, 0),
				LocalDateTime.of(2026, 8, 1, 21, 0),
				null
		));

		FinalScheduleDto.FinalScheduleResponse response = finalScheduleService.updateFinalSchedule(
				target.getId(),
				new FinalScheduleDto.FinalScheduleUpdateRequest(
						LocalDateTime.of(2026, 8, 1, 19, 0),
						LocalDateTime.of(2026, 8, 1, 20, 0),
						"시간 변경"
				)
		);

		assertThat(response.startDateTime()).isEqualTo(LocalDateTime.of(2026, 8, 1, 19, 0));
		assertThat(response.memo()).isEqualTo("시간 변경");

		assertThatThrownBy(() -> finalScheduleService.updateFinalSchedule(
				target.getId(),
				new FinalScheduleDto.FinalScheduleUpdateRequest(
						LocalDateTime.of(2026, 8, 1, 20, 30),
						LocalDateTime.of(2026, 8, 1, 21, 30),
						null
				)
		))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("Final schedule overlaps with another final schedule");
	}

	@Test
	void deleteFinalScheduleRemovesSchedule() {
		Team team = createTeamWithScheduleWindow();
		FinalSchedule finalSchedule = finalScheduleRepository.save(FinalSchedule.create(
				team,
				LocalDateTime.of(2026, 8, 1, 18, 0),
				LocalDateTime.of(2026, 8, 1, 19, 0),
				null
		));

		finalScheduleService.deleteFinalSchedule(finalSchedule.getId());

		assertThat(finalScheduleRepository.findById(finalSchedule.getId())).isEmpty();
	}

	private Team createTeamWithScheduleWindow() {
		return teamRepository.save(Team.create(createPerformance(), "Team A", "Song A"));
	}

	private Performance createPerformance() {
		return performanceRepository.save(Performance.create(
				"2026 Summer Concert",
				LocalDate.of(2026, 8, 20),
				"Main Hall",
				LocalDate.of(2026, 8, 1),
				LocalDate.of(2026, 8, 20)
		));
	}
}
