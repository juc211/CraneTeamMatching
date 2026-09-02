package io.github.juc211.band_schedule.service;

import io.github.juc211.band_schedule.domain.Club;
import io.github.juc211.band_schedule.dto.ClubDto;
import io.github.juc211.band_schedule.exception.BusinessException;
import io.github.juc211.band_schedule.exception.ErrorCode;
import io.github.juc211.band_schedule.repository.ClubRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ClubService {

	private final ClubRepository clubRepository;
	private final PerformanceService performanceService;

	public ClubDto.ClubResponse createClub(ClubDto.ClubCreateRequest request) {
		return toClubResponse(clubRepository.save(Club.create(request.name())));
	}

	@Transactional(readOnly = true)
	public List<ClubDto.ClubResponse> getClubs() {
		return clubRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
				.stream()
				.map(this::toClubResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public ClubDto.ClubResponse getCurrentClub(String adminToken) {
		Club club = clubRepository.findByAdminToken(adminToken)
				.orElseThrow(() -> new BusinessException(ErrorCode.CLUB_ADMIN_UNAUTHORIZED, "Club admin token is missing or invalid"));
		return toClubResponse(club);
	}

	public ClubDto.ClubResponse reissueCurrentClubAdminToken(String adminToken) {
		Club club = clubRepository.findByAdminToken(adminToken)
				.orElseThrow(() -> new BusinessException(ErrorCode.CLUB_ADMIN_UNAUTHORIZED, "Club admin token is missing or invalid"));
		club.reissueAdminToken();
		return toClubResponse(club);
	}

	public void deleteClub(Long clubId) {
		Club club = clubRepository.findById(clubId)
				.orElseThrow(() -> new BusinessException(ErrorCode.CLUB_NOT_FOUND, "Club not found: " + clubId));
		performanceService.deletePerformancesByClub(clubId);
		clubRepository.delete(club);
	}

	private ClubDto.ClubResponse toClubResponse(Club club) {
		return new ClubDto.ClubResponse(
				club.getId(),
				club.getName(),
				club.getAdminToken(),
				"/admin/clubs/" + club.getAdminToken(),
				club.getCreatedAt()
		);
	}
}
