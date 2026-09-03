package io.github.juc211.band_schedule.support;

import io.github.juc211.band_schedule.domain.Club;
import io.github.juc211.band_schedule.domain.Performance;
import io.github.juc211.band_schedule.repository.ClubRepository;
import java.time.LocalDate;

public final class TestEntityFactory {

	private TestEntityFactory() {
	}

	public static Club createClub(ClubRepository clubRepository) {
		return clubRepository.save(Club.create("Test Club"));
	}

	public static Performance createPerformance(
			ClubRepository clubRepository,
			String title,
			LocalDate performanceDate,
			String location
	) {
		return Performance.create(createClub(clubRepository), title, performanceDate, location);
	}

	public static Performance createPerformance(
			ClubRepository clubRepository,
			String title,
			LocalDate performanceDate,
			String location,
			LocalDate scheduleWindowStartDate,
			LocalDate scheduleWindowEndDate
	) {
		return Performance.create(
				createClub(clubRepository),
				title,
				performanceDate,
				location,
				scheduleWindowStartDate,
				scheduleWindowEndDate
		);
	}
}
