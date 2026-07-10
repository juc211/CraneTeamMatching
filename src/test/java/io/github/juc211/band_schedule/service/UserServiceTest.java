package io.github.juc211.band_schedule.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.juc211.band_schedule.domain.Performance;
import io.github.juc211.band_schedule.domain.PerformanceMember;
import io.github.juc211.band_schedule.domain.User;
import io.github.juc211.band_schedule.domain.UserStatus;
import io.github.juc211.band_schedule.dto.UserDto;
import io.github.juc211.band_schedule.repository.PerformanceMemberRepository;
import io.github.juc211.band_schedule.repository.PerformanceRepository;
import io.github.juc211.band_schedule.repository.UserRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class UserServiceTest {

	@Autowired
	private UserService userService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PerformanceRepository performanceRepository;

	@Autowired
	private PerformanceMemberRepository performanceMemberRepository;

	@Test
	void createUserPersistsUserAndReturnsResponse() {
		UserDto.UserCreateRequest request = new UserDto.UserCreateRequest("Kim Band", "20261234");

		UserDto.UserCreateResponse response = userService.createUser(request);

		User savedUser = userRepository.findById(response.userId()).orElseThrow();
		assertThat(savedUser.getName()).isEqualTo("Kim Band");
		assertThat(savedUser.getStudentNumber()).isEqualTo("20261234");
		assertThat(savedUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
		assertThat(response.name()).isEqualTo("Kim Band");
		assertThat(response.studentNumber()).isEqualTo("20261234");
		assertThat(response.status()).isEqualTo(UserStatus.ACTIVE);
	}

	@Test
	void createUserRejectsDuplicateStudentNumber() {
		userService.createUser(new UserDto.UserCreateRequest("Kim Band", "20261234"));

		assertThatThrownBy(() -> userService.createUser(new UserDto.UserCreateRequest("Lee Bass", "20261234")))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Student number already exists");
	}

	@Test
	void getUsersReturnsRegisteredUsers() {
		userService.createUser(new UserDto.UserCreateRequest("Kim Band", "20261234"));
		userService.createUser(new UserDto.UserCreateRequest("Lee Bass", "20261235"));

		assertThat(userService.getUsers())
				.extracting(UserDto.UserResponse::name)
				.containsExactly("Kim Band", "Lee Bass");
	}

	@Test
	void getUsersByStatusReturnsUsersWithStatus() {
		UserDto.UserCreateResponse firstUser = userService.createUser(new UserDto.UserCreateRequest("Kim Band", "20261234"));
		userService.createUser(new UserDto.UserCreateRequest("Lee Bass", "20261235"));
		userService.updateUserStatus(firstUser.userId(), new UserDto.UserStatusUpdateRequest(UserStatus.ON_LEAVE));

		assertThat(userService.getUsersByStatus(UserStatus.ON_LEAVE))
				.extracting(UserDto.UserResponse::name)
				.containsExactly("Kim Band");
		assertThat(userService.getUsersByStatus(UserStatus.ACTIVE))
				.extracting(UserDto.UserResponse::name)
				.containsExactly("Lee Bass");
	}

	@Test
	void getUserReturnsSingleRegisteredUser() {
		UserDto.UserCreateResponse createdUser = userService.createUser(
				new UserDto.UserCreateRequest("Kim Band", "20261234")
		);

		UserDto.UserResponse response = userService.getUser(createdUser.userId());

		assertThat(response.userId()).isEqualTo(createdUser.userId());
		assertThat(response.name()).isEqualTo("Kim Band");
		assertThat(response.studentNumber()).isEqualTo("20261234");
		assertThat(response.status()).isEqualTo(UserStatus.ACTIVE);
	}

	@Test
	void updateUserChangesNameAndStudentNumber() {
		UserDto.UserCreateResponse createdUser = userService.createUser(
				new UserDto.UserCreateRequest("Kim Band", "20261234")
		);

		UserDto.UserResponse response = userService.updateUser(
				createdUser.userId(),
				new UserDto.UserUpdateRequest("Kim Vocal", "20269999")
		);

		User savedUser = userRepository.findById(createdUser.userId()).orElseThrow();
		assertThat(response.name()).isEqualTo("Kim Vocal");
		assertThat(response.studentNumber()).isEqualTo("20269999");
		assertThat(savedUser.getName()).isEqualTo("Kim Vocal");
		assertThat(savedUser.getStudentNumber()).isEqualTo("20269999");
	}

	@Test
	void updateUserRejectsDuplicateStudentNumber() {
		UserDto.UserCreateResponse firstUser = userService.createUser(
				new UserDto.UserCreateRequest("Kim Band", "20261234")
		);
		userService.createUser(new UserDto.UserCreateRequest("Lee Bass", "20261235"));

		assertThatThrownBy(() -> userService.updateUser(
				firstUser.userId(),
				new UserDto.UserUpdateRequest("Kim Band", "20261235")
		))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Student number already exists");
	}

	@Test
	void updateUserStatusChangesStatus() {
		UserDto.UserCreateResponse createdUser = userService.createUser(
				new UserDto.UserCreateRequest("Kim Band", "20261234")
		);

		UserDto.UserResponse response = userService.updateUserStatus(
				createdUser.userId(),
				new UserDto.UserStatusUpdateRequest(UserStatus.GRADUATED)
		);

		assertThat(response.status()).isEqualTo(UserStatus.GRADUATED);
		assertThat(userRepository.findById(createdUser.userId()).orElseThrow().getStatus()).isEqualTo(UserStatus.GRADUATED);
	}

	@Test
	void deleteUserRemovesUserWithoutReferences() {
		UserDto.UserCreateResponse createdUser = userService.createUser(
				new UserDto.UserCreateRequest("Kim Band", "20261234")
		);

		userService.deleteUser(createdUser.userId());

		assertThat(userRepository.findById(createdUser.userId())).isEmpty();
	}

	@Test
	void deleteUserRejectsUserWithPerformanceMemberReference() {
		User user = userRepository.save(User.create("Kim Band", "20261234"));
		Performance performance = performanceRepository.save(
				Performance.create("2026 Summer Concert", LocalDate.of(2026, 8, 20), "Main Hall")
		);
		performanceMemberRepository.save(PerformanceMember.create(performance, user));

		assertThatThrownBy(() -> userService.deleteUser(user.getId()))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("User with references cannot be deleted");
	}
}
