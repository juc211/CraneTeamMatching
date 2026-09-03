package io.github.juc211.band_schedule.controller;

import io.github.juc211.band_schedule.domain.Club;
import io.github.juc211.band_schedule.dto.ClubDto;
import io.github.juc211.band_schedule.dto.PerformanceDto;
import io.github.juc211.band_schedule.dto.UserDto;
import io.github.juc211.band_schedule.service.AdminAuthService;
import io.github.juc211.band_schedule.service.ClubService;
import io.github.juc211.band_schedule.service.PerformanceService;
import io.github.juc211.band_schedule.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminClubController {

	private final AdminAuthService adminAuthService;
	private final ClubService clubService;
	private final PerformanceService performanceService;
	private final UserService userService;

	@PostMapping("/clubs")
	public ResponseEntity<ClubDto.ClubResponse> createClub(
			@RequestHeader(value = "X-Master-Admin-Token", required = false) String masterAdminToken,
			@Valid @RequestBody ClubDto.ClubCreateRequest request
	) {
		adminAuthService.requireMasterAdmin(masterAdminToken);
		return ResponseEntity.status(HttpStatus.CREATED).body(clubService.createClub(request));
	}

	@GetMapping("/clubs")
	public ResponseEntity<List<ClubDto.ClubResponse>> getClubs(
			@RequestHeader(value = "X-Master-Admin-Token", required = false) String masterAdminToken
	) {
		adminAuthService.requireMasterAdmin(masterAdminToken);
		return ResponseEntity.ok(clubService.getClubs());
	}

	@DeleteMapping("/clubs/{clubId}")
	public ResponseEntity<Void> deleteClub(
			@RequestHeader(value = "X-Master-Admin-Token", required = false) String masterAdminToken,
			@PathVariable Long clubId
	) {
		adminAuthService.requireMasterAdmin(masterAdminToken);
		clubService.deleteClub(clubId);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/users")
	public ResponseEntity<List<UserDto.UserResponse>> getUsers(
			@RequestHeader(value = "X-Master-Admin-Token", required = false) String masterAdminToken
	) {
		adminAuthService.requireMasterAdmin(masterAdminToken);
		return ResponseEntity.ok(userService.getUsers());
	}

	@PostMapping("/users")
	public ResponseEntity<UserDto.UserCreateResponse> createUser(
			@RequestHeader(value = "X-Master-Admin-Token", required = false) String masterAdminToken,
			@Valid @RequestBody UserDto.UserCreateRequest request
	) {
		adminAuthService.requireMasterAdmin(masterAdminToken);
		return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
	}

	@PatchMapping("/users/{userId}")
	public ResponseEntity<UserDto.UserResponse> updateUser(
			@RequestHeader(value = "X-Master-Admin-Token", required = false) String masterAdminToken,
			@PathVariable Long userId,
			@Valid @RequestBody UserDto.UserUpdateRequest request
	) {
		adminAuthService.requireMasterAdmin(masterAdminToken);
		return ResponseEntity.ok(userService.updateUser(userId, request));
	}

	@GetMapping("/clubs/current")
	public ResponseEntity<ClubDto.ClubResponse> getCurrentClub(
			@RequestHeader(value = "X-Club-Admin-Token", required = false) String clubAdminToken
	) {
		adminAuthService.requireClubAdmin(clubAdminToken);
		return ResponseEntity.ok(clubService.getCurrentClub(clubAdminToken));
	}

	@PatchMapping("/clubs/current/admin-token")
	public ResponseEntity<ClubDto.ClubResponse> reissueCurrentClubAdminToken(
			@RequestHeader(value = "X-Club-Admin-Token", required = false) String clubAdminToken
	) {
		adminAuthService.requireClubAdmin(clubAdminToken);
		return ResponseEntity.ok(clubService.reissueCurrentClubAdminToken(clubAdminToken));
	}

	@GetMapping("/clubs/current/performances")
	public ResponseEntity<List<PerformanceDto.PerformanceResponse>> getCurrentClubPerformances(
			@RequestHeader(value = "X-Club-Admin-Token", required = false) String clubAdminToken
	) {
		Club club = adminAuthService.requireClubAdmin(clubAdminToken);
		return ResponseEntity.ok(performanceService.getPerformancesByClub(club.getId()));
	}

	@PostMapping("/clubs/current/performances")
	public ResponseEntity<PerformanceDto.PerformanceCreateResponse> createCurrentClubPerformance(
			@RequestHeader(value = "X-Club-Admin-Token", required = false) String clubAdminToken,
			@Valid @RequestBody PerformanceDto.PerformanceCreateRequest request
	) {
		Club club = adminAuthService.requireClubAdmin(clubAdminToken);
		return ResponseEntity.status(HttpStatus.CREATED).body(performanceService.createPerformance(club.getId(), request));
	}
}
