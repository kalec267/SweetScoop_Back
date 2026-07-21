package com.sweetscoop.admin.repository;

import com.sweetscoop.admin.entity.BranchManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BranchManagerRepository extends JpaRepository<BranchManager, String> {
    // 💡 로그인 아이디(login_id)로 점주 정보를 조회하는 메서드
    Optional<BranchManager> findByLoginId(String loginId);

	Optional<BranchManager> findByBranchId(Integer id);

	List<BranchManager> findAllByBranchId(Integer id);
}