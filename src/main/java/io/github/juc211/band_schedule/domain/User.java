package io.github.juc211.band_schedule.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	//8개 숫자로 이루어진 학번
	private String studentNumber;

	public static User create(String name, String studentNumber) {
		User user = new User();
		user.name = name;
		user.studentNumber = studentNumber;
		return user;
	}

	public void update(String name, String studentNumber) {
		this.name = name;
		this.studentNumber = studentNumber;
	}
}
