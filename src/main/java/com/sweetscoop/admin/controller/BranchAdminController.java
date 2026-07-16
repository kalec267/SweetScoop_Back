package com.sweetscoop.admin.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sweetscoop.admin.dto.request.BranchSaveRequestDto;
import com.sweetscoop.admin.dto.response.BranchResponse;
import com.sweetscoop.admin.entity.BranchInventory;
import com.sweetscoop.admin.repository.BranchInventoryRepository;
import com.sweetscoop.admin.repository.HqInventoryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/admin/branches")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
public class BranchAdminController {
	
	private final BranchInventoryRepository branchInventoryRepository;
    private final HqInventoryRepository hqInventoryRepository;
	
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
    
 // 1. 특정 지점의 실시간 원자재 재고 조회 (AS-002)
    @GetMapping("/{branchId}/inventory")
    public ResponseEntity<List<BranchInventory>> getBranchInventory(@PathVariable Integer branchId) {
        log.info("[Branch API] 지점 재고 조회 요청 수신 - 지점 ID: {}", branchId);
        
        // 이전에 복합키와 함께 구현한 fetch join 쿼리 사용
        List<BranchInventory> inventory = branchInventoryRepository.findByBranchIdWithItem(branchId);
        
        log.info("[Branch API] 지점 재고 조회 완료 - 조회된 아이템 수: {}개", inventory.size());
        return ResponseEntity.ok(inventory);
    }

    // 2. 본사로 재고 신청 (발주 등록 - AS-005)
    @PostMapping("/orders")
    public ResponseEntity<String> createOrderRequest(@RequestBody Map<String, Object> payload) {
        log.info("[Branch API] 본사 발주 신청 요청 수신 - Payload: {}", payload);

        // 프론트엔드에서 넘겨준 파라미터 파싱
        Integer branchId = Integer.parseInt(payload.get("branchId").toString());
        Integer itemId = Integer.parseInt(payload.get("itemId").toString());
        Integer quantity = Integer.parseInt(payload.get("quantity").toString());

        log.info("[Branch API] 발주 데이터 파싱 성공 -> 지점: {}, 아이템: {}, 수량: {}개", branchId, itemId, quantity);

        // DB에 발주 데이터(HqInventory 등) 저장 트랜잭션 수행 로직 위치
        // hqInventoryRepository.save(...);

        // 실시간 알림 시스템 트리거 (이전 단계에서 작성한 SSE 호출)
        SseController.sendNotification("스윗스쿱 강남역점에서 새로운 재고 신청이 등록되었습니다!");
        log.info("[Branch API] 본사 관리자 대상 실시간 SSE 알림 브로드캐스트 발송 완료");

        return ResponseEntity.ok("발주 신청이 성공적으로 등록되었습니다.");
    }
}