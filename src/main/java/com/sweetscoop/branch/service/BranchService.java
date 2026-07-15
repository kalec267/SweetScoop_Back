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
    
    public List<BranchDto> getAllBranchesWithStatus() {
        List<Branch> branches = branchRepository.findAll();

        return branches.stream().map(branch -> {
            BranchDto dto = new BranchDto();
            dto.setId(branch.getId());
            dto.setBranchName(branch.getBranchName());
            dto.setLocation(branch.getLocation());
            
            List<KioskDto> kioskDtos = branch.getKiosks().stream()
                    .map(KioskDto::new)
                    .toList();
            dto.setKiosks(kioskDtos);
            
            dto.setStatus(calculateBranchStatus(branch.getKiosks()));
            
            return dto;
        }).toList();
    }

    private String calculateBranchStatus(List<Kiosk> kiosks) {
        if (kiosks == null || kiosks.isEmpty()) {
            return "기기 없음";
        }

        long normalCount = kiosks.stream().filter(k -> "정상".equals(k.getStatus())).count();
        long errorCount = kiosks.stream().filter(k -> "장애".equals(k.getStatus())).count();
        long offCount = kiosks.stream().filter(k -> "꺼짐".equals(k.getStatus())).count();

        if (normalCount == kiosks.size()) {
            return "정상";
        } else if (errorCount == kiosks.size() || offCount == kiosks.size() || (errorCount + offCount) == kiosks.size()) {
            return "중단(폐업/장애)";
        } else {
            return "대기(일부장애/휴무)";
        }
    }
    
    public BranchDto getBranchDetail(Integer id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 분점이 존재하지 않습니다. id=" + id));

        BranchDto dto = new BranchDto();
        dto.setId(branch.getId());
        dto.setBranchName(branch.getBranchName());
        dto.setLocation(branch.getLocation());

        List<KioskDto> kioskDtos = branch.getKiosks().stream()
                .map(KioskDto::new)
                .toList();
        dto.setKiosks(kioskDtos);
        dto.setStatus(calculateBranchStatus(branch.getKiosks()));

        return dto;
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