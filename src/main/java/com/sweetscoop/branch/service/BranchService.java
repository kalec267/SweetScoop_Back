package com.sweetscoop.branch.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sweetscoop.admin.entity.BranchManager;
import com.sweetscoop.admin.repository.BranchManagerRepository;
import com.sweetscoop.branch.dto.BranchDto;
import com.sweetscoop.branch.entity.Branch;
import com.sweetscoop.branch.repository.BranchRepository;
import com.sweetscoop.kiosk.dto.KioskDto;
import com.sweetscoop.kiosk.entity.Kiosk;

@Service
@Transactional(readOnly = true)
public class BranchService {

	@Autowired
	private BranchRepository branchRepository;

	@Autowired
	private BranchManagerRepository branchManagerRepository; // 💡 추가

	private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	/**
	 * 전체 분점 목록 및 통합 상태 조회
	 */
	public List<BranchDto> getAllBranchesWithStatus() {
		List<Branch> branches = branchRepository.findAll();
		return branches.stream().map(this::convertToDto).toList();
	}

	/**
	 * 특정 분점 상세 정보 조회
	 */
	public BranchDto getBranchDetail(Integer id) {
		Branch branch = branchRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("해당 분점이 존재하지 않습니다. id=" + id));

		BranchDto dto = new BranchDto();
		dto.setId(branch.getId());
		dto.setBranchName(branch.getBranchName());
		dto.setLocation(branch.getLocation());

		if (branch.getKiosks() != null) {
			dto.setKiosks(branch.getKiosks().stream().map(KioskDto::new).toList());
			dto.setStatus(calculateBranchStatus(branch.getKiosks()));
		}

		// 💡 해당 지점의 모든 점주 계정 조회 후 DTO 세팅
		List<BranchManager> managers = branchManagerRepository.findAllByBranchId(id);
		dto.setManagers(managers);

		return dto;
	}

	/**
	 * Entity -> DTO 변환 및 상태 계산 공통 메서드
	 */
	private BranchDto convertToDto(Branch branch) {
		BranchDto dto = new BranchDto();
		dto.setId(branch.getId());
		dto.setBranchName(branch.getBranchName());
		dto.setLocation(branch.getLocation());

		List<KioskDto> kioskDtos = branch.getKiosks().stream().map(KioskDto::new).toList();
		dto.setKiosks(kioskDtos);

		// 다변화된 키오스크 상태 기반으로 통합 상태 계산
		dto.setStatus(calculateBranchStatus(branch.getKiosks()));

		return dto;
	}

	/**
	 * 세분화된 키오스크 상태(정상, 점검, 장애, 꺼짐)에 따른 분점 통합 상태 산출
	 */
	private String calculateBranchStatus(List<Kiosk> kiosks) {
		if (kiosks == null || kiosks.isEmpty()) {
			return "기기 없음";
		}

		int totalCount = kiosks.size();

		long normalCount = kiosks.stream().filter(k -> "정상".equals(k.getStatus())).count();
		long inspectionCount = kiosks.stream().filter(k -> "점검".equals(k.getStatus())).count();
		long errorCount = kiosks.stream().filter(k -> "장애".equals(k.getStatus())).count();
		long offCount = kiosks.stream().filter(k -> "꺼짐".equals(k.getStatus())).count();

		// 1. 모든 기기가 정상인 경우
		if (normalCount == totalCount) {
			return "정상";
		}

		// 2. 모든 기기가 장애 또는 꺼짐 상태인 경우 (운영 완전 불가)
		if ((errorCount + offCount) == totalCount) {
			return "중단";
		}

		// 3. 점검 중인 기기가 존재하거나 전 기기가 점검 중인 경우
		if (inspectionCount == totalCount) {
			return "점검 중";
		}

		// 4. 정상 기기와 일부 장애/꺼짐/점검 기기가 섞여 있는 경우
		return "대기(일부장애/휴무)";
	}

	@Transactional
	public void updateBranchManager(Integer branchId, String managerId, String loginId, String name, String password) {
		BranchManager manager;

		// 1. 기존 점주 수정 모드 (managerId가 존재하는 경우)
		if (managerId != null && !managerId.trim().isEmpty()) {
			manager = branchManagerRepository.findById(managerId)
					.orElseThrow(() -> new IllegalArgumentException("해당 점주 계정이 존재하지 않습니다. id=" + managerId));
		}
		// 2. 신규 점주 등록 모드 (managerId가 없는 경우)
		else {
			manager = new BranchManager();

			// 고유 PK ID 생성 (중복 방지를 위한 UUID 또는 카운트 기반 ID)
			String newId = "BM_" + String.format("%03d", System.currentTimeMillis() % 10000);
			manager.setId(newId);
			manager.setBranchId(branchId);
		}

		// 아이디 중복 체크 (기존 내 아이디와 다를 때만 검사)
		if (manager.getLoginId() != null && !manager.getLoginId().equals(loginId)) {
			if (branchManagerRepository.findByLoginId(loginId).isPresent()) {
				throw new RuntimeException("이미 사용 중인 로그인 아이디입니다.");
			}
		}

		manager.setLoginId(loginId);
		manager.setName(name);

		// 비밀번호 입력 시에만 BCrypt 암호화 적용
		if (password != null && !password.trim().isEmpty()) {
			manager.setPassword(passwordEncoder.encode(password));
		}

		branchManagerRepository.save(manager);
	}

	@Transactional
	public void registerBranch(Branch branch) {
		branchRepository.save(branch);
	}

	@Transactional
	public void updateBranch(Integer id, Branch updatedBranch) {
		Branch branch = branchRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("해당 분점이 존재하지 않습니다. id=" + id));

		branch.setBranchName(updatedBranch.getBranchName());
		branch.setLocation(updatedBranch.getLocation());
	}

	@Transactional
	public void deleteBranch(Integer id) {
		Branch branch = branchRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("해당 분점이 존재하지 않습니다. id=" + id));

		// 💡 외래키 제약조건 방지를 위해 자식 테이블(점주 계정) 먼저 전체 삭제
		List<BranchManager> managers = branchManagerRepository.findAllByBranchId(id);
		if (managers != null && !managers.isEmpty()) {
			branchManagerRepository.deleteAll(managers);
		}

		// 부모 테이블(분점) 삭제
		branchRepository.delete(branch);
	}

	@Transactional
	public void deleteBranchManager(String managerId) {
		branchManagerRepository.deleteById(managerId);
	}
}