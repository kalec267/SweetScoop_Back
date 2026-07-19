package com.sweetscoop.branch.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.sweetscoop.branch.dto.BranchDto;
import com.sweetscoop.branch.entity.Branch;
import com.sweetscoop.branch.service.BranchService;

@RestController
@RequestMapping("/api/admin/branches")
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

    //분점 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBranch(@PathVariable("id") Integer id) {
        branchService.deleteBranch(id);
        return ResponseEntity.ok("분점 삭제 성공");
    }
    
    //분점 상세보기
    @GetMapping("/{id}")
    public ResponseEntity<BranchDto> getBranchDetail(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(branchService.getBranchDetail(id));
    }
    
}
