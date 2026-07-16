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
import com.sweetscoop.admin.entity.HqInventory;
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
	// 💡 [수정 완료] 빈 등록 오류를 유발하던 불필요한 HqInventory 필드 선언을 완전히 삭제했습니다.
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

        // 1. 파라미터 파싱
        Integer branchId = Integer.parseInt(payload.get("branchId").toString());
        Integer itemId = Integer.parseInt(payload.get("itemId").toString());
        Integer quantity = Integer.parseInt(payload.get("quantity").toString());

        try {
            // 2. 기존 지점 재고 조회 쿼리를 통해 영속화된 객체 정보 획득
            List<BranchInventory> tempInventory = branchInventoryRepository.findByBranchIdWithItem(branchId);
            if (tempInventory.isEmpty()) {
                return ResponseEntity.status(400).body("해당 지점의 재고 정보가 없어 발주 처리가 불가능합니다.");
            }

            com.sweetscoop.admin.entity.Branch targetBranch = tempInventory.get(0).getBranch();

            com.sweetscoop.admin.entity.Item targetItem = tempInventory.stream()
                    .map(BranchInventory::getItem)
                    .filter(item -> item.getId().equals(itemId))
                    .findFirst()
                    .orElse(null);

            if (targetItem == null) {
                return ResponseEntity.status(400).body("지점 재고 정보에 등록되지 않은 원자재 아이템 코드입니다.");
            }

            // 3. 빌더 패턴을 사용하여 HqInventory 객체 구축 (role 조건 없이 데이터 삽입에만 집중)
            com.sweetscoop.admin.entity.HqInventory hqOrder = com.sweetscoop.admin.entity.HqInventory.builder()
                    .branch(targetBranch)             
                    .item(targetItem)                 
                    .requestQuantity(quantity)        
                    .approvalStatus("대기중")         // 기본 상태 지정
                    .deliveryStatus("준비중")         // 기본 상태 지정
                    .build();

            // 4. DB INSERT 실행
            hqInventoryRepository.save(hqOrder);
            log.info("[Branch API] DB 저장 성공 - 생성된 발주 ID: {}", hqOrder.getId());

            // 5. 실시간 SSE 알림 전송
            SseController.sendNotification("스윗스쿱 강남역점에서 새로운 재고 신청이 등록되었습니다!");

            return ResponseEntity.ok("발주 신청이 성공적으로 등록되었습니다.");
            
        } catch (Exception e) {
            log.error("[Branch API] 발주 저장 중 예외 발생!", e);
            return ResponseEntity.status(500).body("서버 내부 저장 오류: " + e.getMessage());
        }
    }
    
    // 3. 특정 지점의 발주(재고 신청) 내역 조회 API 추가
    @GetMapping("/{branchId}/orders")
    public ResponseEntity<List<HqInventory>> getBranchOrders(@PathVariable Integer branchId) {
        log.info("[Branch API] 지점 발주 내역 조회 요청 수신 - 지점 ID: {}", branchId);
        
        // 💡 [수정 완료] JPA 엔티티 속성에 정확히 부합하도록 findByBranch_Id 조회 방식으로 매핑 동기화
        List<HqInventory> orders = hqInventoryRepository.findByBranch_Id(branchId);
        
        log.info("[Branch API] 지점 발주 내역 조회 완료 - 조회된 발주 건수: {}개", orders.size());
        return ResponseEntity.ok(orders);
    }
}