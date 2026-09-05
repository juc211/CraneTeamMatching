package io.github.juc211.band_schedule.service;

import io.github.juc211.band_schedule.domain.InputLink;
import io.github.juc211.band_schedule.domain.MemberAccessSession;
import io.github.juc211.band_schedule.domain.PerformanceMember;
import io.github.juc211.band_schedule.domain.TeamMember;
import io.github.juc211.band_schedule.exception.BusinessException;
import io.github.juc211.band_schedule.exception.ErrorCode;
import io.github.juc211.band_schedule.repository.MemberAccessSessionRepository;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberAccessSessionService {

	private static final int TOKEN_BYTE_LENGTH = 32;
	private static final long SESSION_TTL_HOURS = 12;

	private final MemberAccessSessionRepository memberAccessSessionRepository;
	private final SecureRandom secureRandom = new SecureRandom();

	/**
	 * 공연 참여 인원 접근 세션 생성
	 */
	public MemberAccessSession createSession(InputLink inputLink, PerformanceMember performanceMember) {
		MemberAccessSession session = MemberAccessSession.create(
				generateUniqueToken(),
				performanceMember,
				inputLink,
				LocalDateTime.now().plusHours(SESSION_TTL_HOURS)
		);
		return memberAccessSessionRepository.save(session);
	}

	/**
	 * 입력 링크와 팀원 소유자 기준 접근 권한 검증
	 */
	@Transactional(readOnly = true)
	public MemberAccessSession requireOwnerSession(String token, InputLink inputLink, TeamMember teamMember) {
		MemberAccessSession session = findValidSession(token);

		if (!session.getInputLink().getId().equals(inputLink.getId())) {
			throw new BusinessException(ErrorCode.MEMBER_ACCESS_UNAUTHORIZED, "Member access token was not issued for this input link");
		}
		if (!session.getPerformanceMember().getPerformance().getId().equals(inputLink.getPerformance().getId())) {
			throw new BusinessException(ErrorCode.MEMBER_ACCESS_UNAUTHORIZED, "Member access token does not belong to link performance");
		}
		if (!session.getPerformanceMember().getId().equals(teamMember.getPerformanceMember().getId())) {
			throw new BusinessException(ErrorCode.MEMBER_ACCESS_FORBIDDEN, "Member cannot access another member resource");
		}

		return session;
	}

	/**
	 * 유효한 접근 세션 조회
	 */
	@Transactional(readOnly = true)
	public MemberAccessSession findValidSession(String token) {
		if (!StringUtils.hasText(token)) {
			throw new BusinessException(ErrorCode.MEMBER_ACCESS_UNAUTHORIZED, "Member access token is missing or invalid");
		}

		MemberAccessSession session = memberAccessSessionRepository.findByToken(token)
				.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_ACCESS_UNAUTHORIZED, "Member access token is missing or invalid"));
		if (session.isExpired()) {
			throw new BusinessException(ErrorCode.MEMBER_ACCESS_UNAUTHORIZED, "Member access token is expired");
		}
		return session;
	}

	/**
	 * 고유 접근 토큰 생성
	 */
	private String generateUniqueToken() {
		String token;
		do {
			byte[] bytes = new byte[TOKEN_BYTE_LENGTH];
			secureRandom.nextBytes(bytes);
			token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
		} while (memberAccessSessionRepository.existsByToken(token));
		return token;
	}
}
