package io.github.juc211.band_schedule.domain;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class DomainEntityMappingTests {

	@Test
	void domainClassesAreJpaEntities() {
		entityTypes().forEach(this::assertEntity);
	}

	@Test
	void entityClassesUseLombokGetterAndProtectedNoArgsConstructor() throws Exception {
		for (Class<?> entityType : entityTypes()) {
			String source = Files.readString(sourcePath(entityType));

			assertThat(source).contains("import lombok.AccessLevel;");
			assertThat(source).contains("import lombok.Getter;");
			assertThat(source).contains("import lombok.NoArgsConstructor;");
			assertThat(source).contains("@Getter");
			assertThat(source).contains("@NoArgsConstructor(access = AccessLevel.PROTECTED)");
			assertThat(source).doesNotContain("protected " + entityType.getSimpleName() + "()");
		}
	}

	@Test
	void aggregateRootsDoNotExposeEntityCollections() {
		assertThat(User.class.getDeclaredFields()).noneMatch(this::isOneToManyField);
		assertThat(Performance.class.getDeclaredFields()).noneMatch(this::isOneToManyField);
	}

	@Test
	void relationshipFieldsUseLazyManyToOne() throws NoSuchFieldException {
		assertLazyManyToOne(PerformanceMember.class, "performance");
		assertLazyManyToOne(PerformanceMember.class, "user");
		assertLazyManyToOne(Team.class, "performance");
		assertLazyManyToOne(TeamMember.class, "team");
		assertLazyManyToOne(TeamMember.class, "performanceMember");
		assertLazyManyToOne(SongRequest.class, "performance");
		assertLazyManyToOne(SongRequest.class, "team");
		assertLazyManyToOne(SongRequest.class, "requestedByMember");
		assertLazyManyToOne(SongVote.class, "songRequest");
		assertLazyManyToOne(SongVote.class, "voterMember");
		assertLazyManyToOne(Availability.class, "teamMember");
		assertLazyManyToOne(FinalSchedule.class, "team");
		assertLazyManyToOne(InputLink.class, "performance");
		assertLazyManyToOne(InputLink.class, "team");
		assertLazyManyToOne(UserSession.class, "user");
	}

	private List<Class<?>> entityTypes() {
		return List.of(
				User.class,
				UserSession.class,
				Performance.class,
				PerformanceMember.class,
				Team.class,
				TeamMember.class,
				SongRequest.class,
				SongVote.class,
				Availability.class,
				FinalSchedule.class,
				InputLink.class
		);
	}

	private Path sourcePath(Class<?> entityType) {
		return Path.of("src/main/java", entityType.getName().replace('.', '/') + ".java");
	}

	private boolean isOneToManyField(Field field) {
		return field.isAnnotationPresent(OneToMany.class);
	}

	private void assertEntity(Class<?> type) {
		assertThat(type.isAnnotationPresent(Entity.class)).isTrue();
	}

	private void assertLazyManyToOne(Class<?> type, String fieldName) throws NoSuchFieldException {
		ManyToOne manyToOne = type.getDeclaredField(fieldName).getAnnotation(ManyToOne.class);
		assertThat(manyToOne).isNotNull();
		assertThat(manyToOne.fetch()).isEqualTo(FetchType.LAZY);
	}
}
