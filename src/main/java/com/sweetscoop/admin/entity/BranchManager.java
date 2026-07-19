package com.sweetscoop.admin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "BRANCHMANAGER")
@Getter
@Setter
@NoArgsConstructor
public class BranchManager {

	@Id
	@Column(name = "id", length = 50)
	private String id; // 점주 고유 식별 ID

	@Column(name = "login_id", nullable = false, unique = true, length = 50)
	private String loginId; // 로그인용 계정 아이디

	@Column(name = "password", nullable = false, length = 100)
	private String password; // 암호화된 비밀번호 (BCrypt 해시값)

	@Column(name = "name", nullable = false, length = 50)
	private String name; // 점주 이름

	@Column(name = "branch_id", nullable = false)
	private Integer branchId; // 담당 지점 ID
}