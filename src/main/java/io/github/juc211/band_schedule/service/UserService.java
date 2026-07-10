package io.github.juc211.band_schedule.service;

import io.github.juc211.band_schedule.domain.User;
import io.github.juc211.band_schedule.domain.UserStatus;
import io.github.juc211.band_schedule.dto.UserDto;
import io.github.juc211.band_schedule.repository.PerformanceMemberRepository;
import io.github.juc211.band_schedule.repository.UserRepository;
import io.github.juc211.band_schedule.repository.UserSessionRepository;
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
	private final PerformanceMemberRepository performanceMemberRepository;
	private final UserSessionRepository userSessionRepository;

	/**
	 * 밴드 멤버 생성
	 */
	public UserDto.UserCreateResponse createUser(UserDto.UserCreateRequest request) {
		validateStudentNumberNotDuplicated(request.studentNumber());

		User user = User.create(request.name(), request.studentNumber());

		User savedUser = userRepository.save(user);

		return new UserDto.UserCreateResponse(
				savedUser.getId(),
				savedUser.getName(),
				savedUser.getStudentNumber(),
				savedUser.getStatus()
		);
	}

	/**
	 * 유저 단건 조회
	 */
	@Transactional(readOnly = true)
	public UserDto.UserResponse getUser(Long userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

		return toUserResponse(user);
	}

	/**
	 * 유저 기본 정보 수정
	 */
	public UserDto.UserResponse updateUser(Long userId, UserDto.UserUpdateRequest request) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

		validateStudentNumberNotDuplicated(userId, request.studentNumber());
		user.update(request.name(), request.studentNumber());

		return toUserResponse(user);
	}

	/**
	 * 유저 전체 조회
	 */
	@Transactional(readOnly = true)
	public List<UserDto.UserResponse> getUsers() {
		return userRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
				.stream()
				.map(this::toUserResponse)
				.toList();
	}

	/**
	 * 유저 상태별 조회(status가 없으면 전체 조회)
	 */
	@Transactional(readOnly = true)
	public List<UserDto.UserResponse> getUsersByStatus(UserStatus status) {
		if (status == null) {
			return getUsers();
		}

		return userRepository.findByStatusOrderByIdAsc(status)
				.stream()
				.map(this::toUserResponse)
				.toList();
	}

	/**
	 * 유저 상태 수정
	 */
	public UserDto.UserResponse updateUserStatus(Long userId, UserDto.UserStatusUpdateRequest request) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

		user.updateStatus(request.status());

		return toUserResponse(user);
	}

	/**
	 * 참조 없는 유저 삭제
	 */
	public void deleteUser(Long userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

		if (performanceMemberRepository.existsByUserId(userId) || userSessionRepository.existsByUserId(userId)) {
			throw new IllegalArgumentException("User with references cannot be deleted. Update the user status instead.");
		}

		userRepository.delete(user);
	}

	/**
	 * 유저 응답 변환
	 */
	private UserDto.UserResponse toUserResponse(User user) {
		return new UserDto.UserResponse(
				user.getId(),
				user.getName(),
				user.getStudentNumber(),
				user.getStatus()
		);
	}

	/**
	 * 학번 중복 여부 검증
	 */
	private void validateStudentNumberNotDuplicated(String studentNumber) {
		if (userRepository.existsByStudentNumber(studentNumber)) {
			throw new IllegalArgumentException("Student number already exists: " + studentNumber);
		}
	}

	/**
	 * 본인을 제외한 학번 중복 여부 검증
	 */
	private void validateStudentNumberNotDuplicated(Long userId, String studentNumber) {
		if (userRepository.existsByStudentNumberAndIdNot(studentNumber, userId)) {
			throw new IllegalArgumentException("Student number already exists: " + studentNumber);
		}
	}
}
