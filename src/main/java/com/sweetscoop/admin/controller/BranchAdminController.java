package com.sweetscoop.admin.controller;

import com.sweetscoop.admin.dto.request.BranchSaveRequestDto;
import com.sweetscoop.admin.dto.response.BranchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/branches")
@RequiredArgsConstructor
public class BranchAdminController {

    // 1. 분점 전체 조회
    @GetMapping
    public ResponseEntity<List<BranchResponse>> getAllBranches() {
        return ResponseEntity.ok(List.of());
    }

    // 2. 분점 등록
    @PostMapping
    public ResponseEntity<String> createBranch(@RequestBody BranchSaveRequestDto dto) {
        return ResponseEntity.ok("신규 분점이 등록되었습니다.");
    }

    // 3. 분점 정보 수정
    @PutMapping("/{id}")
    public ResponseEntity<String> updateBranch(@PathVariable Integer id, @RequestBody BranchSaveRequestDto dto) {
        return ResponseEntity.ok("분점 정보가 업데이트되었습니다.");
    }

    // 4. 분점 영업 상태 관리 (예: 영업중 / 휴업 / 폐업 등 임시 상태 패치)
    @PatchMapping("/{id}/status")
    public ResponseEntity<String> updateBranchStatus(@PathVariable Integer id, @RequestParam String status) {
        return ResponseEntity.ok("분점 영업 상태가 [" + status + "](으)로 변경되었습니다.");
    }

    // 5. 분점 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBranch(@PathVariable Integer id) {
        return ResponseEntity.ok("분점 정보가 삭제되었습니다.");
    }
}