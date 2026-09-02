package io.github.juc211.band_schedule.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
/**
 * 밴드 멤버 전원(동아리 세션 인원 전원) - 인원 변경 시 db 수정
 */
public class User {
	private static final String STUDENT_NUMBER_PATTERN = "\\d{8}";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 50)
	private String name;

	//8개 숫자로 이루어진 학번
	@Column(nullable = false, length = 8, unique = true)
	private String studentNumber;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private UserStatus status;

	public static User create(String name, String studentNumber) {
		validateStudentNumber(studentNumber);
		User user = new User();
		user.name = name;
		user.studentNumber = studentNumber;
		user.status = UserStatus.ACTIVE;
		return user;
	}

	public void update(String name, String studentNumber) {
		validateStudentNumber(studentNumber);
		this.name = name;
		this.studentNumber = studentNumber;
	}

	public void updateStatus(UserStatus status) {
		if (status == null) {
			throw new IllegalArgumentException("User status is required");
		}
		this.status = status;
	}

	private static void validateStudentNumber(String studentNumber) {
		if (studentNumber == null || !studentNumber.matches(STUDENT_NUMBER_PATTERN)) {
			throw new IllegalArgumentException("Student number must be exactly 8 digits");
		}
	}
}
