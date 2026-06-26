package io.github.juc211.band_schedule.repository;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.juc211.band_schedule.domain.Availability;
import io.github.juc211.band_schedule.domain.FinalSchedule;
import io.github.juc211.band_schedule.domain.InputLink;
import io.github.juc211.band_schedule.domain.SongVote;
import java.lang.reflect.ParameterizedType;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;

class RepositoryDefinitionTests {

	@Test
	void remainingEntityRepositoriesExtendJpaRepositoryWithLongId() {
		assertJpaRepository(AvailabilityRepository.class, Availability.class);
		assertJpaRepository(FinalScheduleRepository.class, FinalSchedule.class);
		assertJpaRepository(InputLinkRepository.class, InputLink.class);
		assertJpaRepository(SongVoteRepository.class, SongVote.class);
	}

	private void assertJpaRepository(Class<?> repositoryType, Class<?> entityType) {
		ParameterizedType repositoryInterface = (ParameterizedType) repositoryType.getGenericInterfaces()[0];

		assertThat(repositoryInterface.getRawType()).isEqualTo(JpaRepository.class);
		assertThat(repositoryInterface.getActualTypeArguments()).containsExactly(entityType, Long.class);
		assertThat(repositoryType.getDeclaredMethods()).isEmpty();
	}
}
