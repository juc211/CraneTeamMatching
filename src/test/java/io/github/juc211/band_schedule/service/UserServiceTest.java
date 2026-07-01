package io.github.juc211.band_schedule.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.juc211.band_schedule.domain.User;
import io.github.juc211.band_schedule.dto.UserDto;
import io.github.juc211.band_schedule.repository.UserRepository;
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

	@Test
	void createUserPersistsUserAndReturnsResponse() {
		UserDto.UserCreateRequest request = new UserDto.UserCreateRequest("Kim Band", "20261234");

		UserDto.UserCreateResponse response = userService.createUser(request);

		User savedUser = userRepository.findById(response.userId()).orElseThrow();
		assertThat(savedUser.getName()).isEqualTo("Kim Band");
		assertThat(savedUser.getStudentNumber()).isEqualTo("20261234");
		assertThat(response.name()).isEqualTo("Kim Band");
		assertThat(response.studentNumber()).isEqualTo("20261234");
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
	void getUserReturnsSingleRegisteredUser() {
		UserDto.UserCreateResponse createdUser = userService.createUser(
				new UserDto.UserCreateRequest("Kim Band", "20261234")
		);

		UserDto.UserResponse response = userService.getUser(createdUser.userId());

		assertThat(response.userId()).isEqualTo(createdUser.userId());
		assertThat(response.name()).isEqualTo("Kim Band");
		assertThat(response.studentNumber()).isEqualTo("20261234");
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
}
