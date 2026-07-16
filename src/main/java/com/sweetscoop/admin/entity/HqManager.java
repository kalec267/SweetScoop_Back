package com.sweetscoop.admin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "HQMANAGER")
@Getter
@Setter
@NoArgsConstructor
public class HqManager {

    @Id
    @Column(name = "id", length = 50)
    private String id; // 관리자 고유 식별 ID

    @Column(name = "login_id", nullable = false, unique = true, length = 50)
    private String loginId; // 로그인용 계정 아이디

    @Column(name = "password", nullable = false, length = 100)
    private String password; // 암호화된 비밀번호 (BCrypt 해시값)

    @Column(name = "name", nullable = false, length = 50)
    private String name; // 관리자 이름
}