package com.sweetscoop.branch.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    
    /**
     * 전체 분점 목록 및 통합 상태 조회
     */
    public List<BranchDto> getAllBranchesWithStatus() {
        List<Branch> branches = branchRepository.findAll();
        return branches.stream()
                .map(this::convertToDto)
                .toList();
    }

    /**
     * 특정 분점 상세 정보 조회
     */
    public BranchDto getBranchDetail(Integer id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 분점이 존재하지 않습니다. id=" + id));

        return convertToDto(branch);
    }

    /**
     * Entity -> DTO 변환 및 상태 계산 공통 메서드
     */
    private BranchDto convertToDto(Branch branch) {
        BranchDto dto = new BranchDto();
        dto.setId(branch.getId());
        dto.setBranchName(branch.getBranchName());
        dto.setLocation(branch.getLocation());
        
        List<KioskDto> kioskDtos = branch.getKiosks().stream()
                .map(KioskDto::new)
                .toList();
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
        branchRepository.delete(branch);
    }
}