package io.github.juc211.band_schedule.service;

import io.github.juc211.band_schedule.domain.User;
import io.github.juc211.band_schedule.dto.UserDto;
import io.github.juc211.band_schedule.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;

	public UserDto.UserCreateResponse createUser(UserDto.UserCreateRequest request) {
		User user = User.create(request.name(), request.studentNumber());

		User savedUser = userRepository.save(user);

		return new UserDto.UserCreateResponse(
				savedUser.getId(),
				savedUser.getName(),
				savedUser.getStudentNumber()
		);
	}

	@Transactional(readOnly = true)
	public UserDto.UserResponse getUser(Long userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

		return toUserResponse(user);
	}

	public UserDto.UserResponse updateUser(Long userId, UserDto.UserUpdateRequest request) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

		user.update(request.name(), request.studentNumber());

		return toUserResponse(user);
	}

	@Transactional(readOnly = true)
	public List<UserDto.UserResponse> getUsers() {
		return userRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
				.stream()
				.map(this::toUserResponse)
				.toList();
	}

	private UserDto.UserResponse toUserResponse(User user) {
		return new UserDto.UserResponse(
				user.getId(),
				user.getName(),
				user.getStudentNumber()
		);
	}
}
