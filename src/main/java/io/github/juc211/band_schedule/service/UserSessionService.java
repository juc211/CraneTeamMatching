package io.github.juc211.band_schedule.service;

import io.github.juc211.band_schedule.domain.User;
import io.github.juc211.band_schedule.domain.UserSession;
import io.github.juc211.band_schedule.dto.UserSessionDto;
import io.github.juc211.band_schedule.exception.BusinessException;
import io.github.juc211.band_schedule.exception.ErrorCode;
import io.github.juc211.band_schedule.repository.UserRepository;
import io.github.juc211.band_schedule.repository.UserSessionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserSessionService {

	private final UserSessionRepository userSessionRepository;
	private final UserRepository userRepository;

	/**
	 * 유저 세션 추가
	 */
	public UserSessionDto.UserSessionResponse createUserSession(
			Long userId,
			UserSessionDto.UserSessionCreateRequest request
	) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "User not found: " + userId));

		UserSession savedUserSession = userSessionRepository.save(UserSession.create(user, request.part()));

		return toUserSessionResponse(savedUserSession);
	}

	/**
	 * 유저 세션 목록 조회
	 */
	@Transactional(readOnly = true)
	public List<UserSessionDto.UserSessionResponse> getUserSessionsByUser(Long userId) {
		if (!userRepository.existsById(userId)) {
			throw new BusinessException(ErrorCode.USER_NOT_FOUND, "User not found: " + userId);
		}

		return userSessionRepository.findByUserId(userId)
				.stream()
				.map(this::toUserSessionResponse)
				.toList();
	}

	/**
	 * 유저 세션 삭제
	 */
	public void deleteUserSession(Long userSessionId) {
		UserSession userSession = userSessionRepository.findById(userSessionId)
				.orElseThrow(() -> new BusinessException(ErrorCode.USER_SESSION_NOT_FOUND, "UserSession not found: " + userSessionId));

		userSessionRepository.delete(userSession);
	}

	/**
	 * 유저 세션 응답 변환
	 */
	private UserSessionDto.UserSessionResponse toUserSessionResponse(UserSession userSession) {
		return new UserSessionDto.UserSessionResponse(
				userSession.getId(),
				userSession.getUser().getId(),
				userSession.getPart()
		);
	}
}
