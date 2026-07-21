package com.sweetscoop.kiosk.controller;

import com.sweetscoop.kiosk.dto.KioskCreateRequestDto;
import com.sweetscoop.kiosk.dto.KioskDto;
import com.sweetscoop.kiosk.service.KioskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class KioskController {

    private final KioskService kioskService;

    // 1. 특정 분점의 키오스크 목록 조회 GET /api/admin/branches/{branchId}/kiosks
    @GetMapping("/branches/{branchId}/kiosks")
    public ResponseEntity<List<KioskDto>> getKiosksByBranch(@PathVariable Integer branchId) {
        List<KioskDto> kiosks = kioskService.getKiosksByBranchId(branchId);
        return ResponseEntity.ok(kiosks);
    }

    // 2. 신규 키오스크 등록 POST /api/admin/branches/{branchId}/kiosks
    @PostMapping("/branches/{branchId}/kiosks")
    public ResponseEntity<KioskDto> createKiosk(
            @PathVariable Integer branchId,
            @RequestBody KioskCreateRequestDto requestDto) {
        KioskDto createdKiosk = kioskService.createKiosk(branchId, requestDto);
        return ResponseEntity.ok(createdKiosk);
    }

    // 3. 키오스크 상태 변경 PATCH /api/admin/kiosks/{kioskId}/status
    @PatchMapping("/kiosks/{kioskId}/status")
    public ResponseEntity<KioskDto> updateKioskStatus(
            @PathVariable Integer kioskId,
            @RequestBody Map<String, String> body) {
        String newStatus = body.get("status");
        KioskDto updatedKiosk = kioskService.updateKioskStatus(kioskId, newStatus);
        return ResponseEntity.ok(updatedKiosk);
    }

    // 4. 키오스크 삭제 DELETE /api/admin/kiosks/{kioskId}
    @DeleteMapping("/kiosks/{kioskId}")
    public ResponseEntity<Void> deleteKiosk(@PathVariable Integer kioskId) {
        kioskService.deleteKiosk(kioskId);
        return ResponseEntity.noContent().build();
    }
}