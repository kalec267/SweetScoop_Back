package com.sweetscoop.kiosk.service;

import com.sweetscoop.branch.entity.Branch;
import com.sweetscoop.branch.repository.BranchRepository;
import com.sweetscoop.kiosk.dto.KioskCreateRequestDto;
import com.sweetscoop.kiosk.dto.KioskDto;
import com.sweetscoop.kiosk.entity.Kiosk;
import com.sweetscoop.kiosk.repository.KioskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KioskService {

    private final KioskRepository kioskRepository;
    private final BranchRepository branchRepository;

    // 특정 분점의 키오스크 목록 조회
    public List<KioskDto> getKiosksByBranchId(Integer branchId) {
        return kioskRepository.findByBranchId(branchId).stream()
                .map(KioskDto::new)
                .collect(Collectors.toList());
    }

    // 키오스크 등록 (AUTO_INCREMENT로 ID 자동 생성)
    @Transactional
    public KioskDto createKiosk(Integer branchId, KioskCreateRequestDto requestDto) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new IllegalArgumentException("해당 분점이 존재하지 않습니다. id=" + branchId));

        Kiosk kiosk = new Kiosk();
        kiosk.setBranch(branch);
        kiosk.setStatus(requestDto.getStatus() != null ? requestDto.getStatus() : "정상");

        Kiosk savedKiosk = kioskRepository.save(kiosk);
        return new KioskDto(savedKiosk);
    }

    // 키오스크 상태 변경
    @Transactional
    public KioskDto updateKioskStatus(Integer kioskId, String status) {
        Kiosk kiosk = kioskRepository.findById(kioskId)
                .orElseThrow(() -> new IllegalArgumentException("해당 키오스크가 존재하지 않습니다. id=" + kioskId));

        kiosk.setStatus(status);
        return new KioskDto(kiosk);
    }

    // 키오스크 삭제
    @Transactional
    public void deleteKiosk(Integer kioskId) {
        Kiosk kiosk = kioskRepository.findById(kioskId)
                .orElseThrow(() -> new IllegalArgumentException("해당 키오스크가 존재하지 않습니다. id=" + kioskId));

        kioskRepository.delete(kiosk);
    }
}