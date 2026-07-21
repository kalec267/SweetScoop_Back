package com.sweetscoop.branch.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sweetscoop.branch.dto.BranchDto;
import com.sweetscoop.branch.entity.Branch;
import com.sweetscoop.branch.service.BranchService;

@RestController
@RequestMapping("/api/admin/branches")
@CrossOrigin(origins = {"http://localhost:5173","http://192.168.137.173:5173", "http://172.30.1.17:5300"})
public class BranchModityController {

    @Autowired
    private BranchService branchService; 

    @GetMapping
    public ResponseEntity<List<BranchDto>> getAllBranches() {
        return ResponseEntity.ok(branchService.getAllBranchesWithStatus());
    }

    //신규 분점 등록
    @PostMapping
    public ResponseEntity<String> registerBranch(@RequestBody Branch branch) {
        branchService.registerBranch(branch);
        return ResponseEntity.ok("분점 등록 성공");
    }

    //분점 정보 수정
    @PutMapping("/{id}")
    public ResponseEntity<String> updateBranch(@PathVariable("id") Integer id, @RequestBody Branch updatedBranch) {
        branchService.updateBranch(id, updatedBranch);
        return ResponseEntity.ok("분점 수정 성공");
    }
    
    // 분점 점주 계정 정보 수정 API (DTO로 받거나 RequestParam/Map 형태)
    @PutMapping("/{id}/manager")
    public ResponseEntity<String> updateBranchManager(
            @PathVariable("id") Integer branchId,
            @RequestBody Map<String, String> requestBody) {

        String managerId = requestBody.get("managerId"); // 💡 추가
        String loginId = requestBody.get("loginId");
        String name = requestBody.get("name");
        String password = requestBody.get("password");

        branchService.updateBranchManager(branchId, managerId, loginId, name, password);
        return ResponseEntity.ok("점주 계정 정보가 성공적으로 저장되었습니다.");
    }
    //분점 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBranch(@PathVariable("id") Integer id) {
        branchService.deleteBranch(id);
        return ResponseEntity.ok("분점 삭제 성공");
    }
    
    // 점주 계정 개별 삭제 API
    @DeleteMapping("/managers/{managerId}")
    public ResponseEntity<String> deleteBranchManager(@PathVariable("managerId") String managerId) {
        branchService.deleteBranchManager(managerId);
        return ResponseEntity.ok("점주 계정이 삭제되었습니다.");
    }
    
    //분점 상세보기
    @GetMapping("/{id}")
    public ResponseEntity<BranchDto> getBranchDetail(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(branchService.getBranchDetail(id));
    }
    
}
