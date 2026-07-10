package io.github.juc211.band_schedule.repository;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.juc211.band_schedule.domain.AvailableTime;
import io.github.juc211.band_schedule.domain.PerformanceConfirmedSong;
import io.github.juc211.band_schedule.domain.FinalSchedule;
import io.github.juc211.band_schedule.domain.InputLink;
import io.github.juc211.band_schedule.domain.PerformanceSetlistItem;
import io.github.juc211.band_schedule.domain.SongPreference;
import io.github.juc211.band_schedule.domain.SongVote;
import java.lang.reflect.ParameterizedType;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;

class RepositoryDefinitionTests {

	@Test
	void remainingEntityRepositoriesExtendJpaRepositoryWithLongId() {
		assertJpaRepositoryWithDeclaredMethodCount(AvailabilityRepository.class, AvailableTime.class, 7);
		assertJpaRepositoryWithDeclaredMethodCount(PerformanceConfirmedSongRepository.class, PerformanceConfirmedSong.class, 2);
		assertJpaRepositoryWithDeclaredMethodCount(PerformanceSetlistItemRepository.class, PerformanceSetlistItem.class, 3);
		assertJpaRepositoryWithDeclaredMethodCount(FinalScheduleRepository.class, FinalSchedule.class, 6);
		assertJpaRepositoryWithDeclaredMethodCount(InputLinkRepository.class, InputLink.class, 4);
		assertJpaRepositoryWithDeclaredMethodCount(SongPreferenceRepository.class, SongPreference.class, 6);
		assertJpaRepositoryWithDeclaredMethodCount(SongVoteRepository.class, SongVote.class, 7);
	}

	private void assertJpaRepository(Class<?> repositoryType, Class<?> entityType) {
		assertJpaRepositoryWithDeclaredMethodCount(repositoryType, entityType, 0);
	}

	private void assertJpaRepositoryWithDeclaredMethodCount(Class<?> repositoryType, Class<?> entityType, int declaredMethodCount) {
		ParameterizedType repositoryInterface = (ParameterizedType) repositoryType.getGenericInterfaces()[0];

		assertThat(repositoryInterface.getRawType()).isEqualTo(JpaRepository.class);
		assertThat(repositoryInterface.getActualTypeArguments()).containsExactly(entityType, Long.class);
		assertThat(repositoryType.getDeclaredMethods()).hasSize(declaredMethodCount);
	}
}
