package com.sweetscoop.admin.repository;

import com.sweetscoop.admin.entity.HqManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HqManagerRepository extends JpaRepository<HqManager, String> {
    // 💡 로그인 아이디(login_id)로 본사 관리자 정보를 조회하는 메서드
    Optional<HqManager> findByLoginId(String loginId);
}