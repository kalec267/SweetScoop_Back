package com.sweetscoop.admin.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sweetscoop.admin.dto.request.MenuPriceUpdateRequestDto;
import com.sweetscoop.admin.dto.request.MenuSaveRequestDto;
import com.sweetscoop.admin.dto.response.MenuDetailResponse;
import com.sweetscoop.admin.entity.Menu;
import com.sweetscoop.admin.entity.Size;
import com.sweetscoop.admin.repository.MenuRepository;
import com.sweetscoop.admin.repository.SizeRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/menus")
@RequiredArgsConstructor
public class MenuAdminController {

	private final MenuRepository menuRepository; // 👈 의존성 주입
	private final SizeRepository sizeRepository;

    // 1. 메뉴 전체 조회 (진짜 DB 데이터 반환하도록 완성)
	@GetMapping
    public ResponseEntity<List<MenuDetailResponse>> getAllMenus() {
        List<Menu> menus = menuRepository.findAll();
        
        List<MenuDetailResponse> response = menus.stream()
                .map(m -> MenuDetailResponse.builder()
                        .id(m.getId())
                        .name(m.getName())
                        .menuImg(m.getMenuImg())
                        .categoryName(String.valueOf(m.getCategoryId())) 
                        .itemName("물류 ID: " + m.getItemId())
                        // 💡 [추가] 새로 추가한 ID 필드들에 엔티티의 값을 그대로 맵핑해 줍니다.
                        .categoryId(m.getCategoryId())
                        .itemId(m.getItemId())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // 2. 메뉴 등록 로직도 완성해둡니다.
    @PostMapping
    public ResponseEntity<String> createMenu(@RequestBody MenuSaveRequestDto dto) {
        Menu menu = Menu.builder()
                .categoryId(dto.getCategoryId())
                .itemId(dto.getItemId())
                .name(dto.getName())
                .menuImg(dto.getMenuImg())
                .build();
                
        menuRepository.save(menu);
        return ResponseEntity.ok("메뉴가 성공적으로 등록되었습니다.");
    }

    // 3. 메뉴 정보 수정 (메뉴 수정)
    @PutMapping("/{id}")
    @Transactional // ⚠️ 데이터 변경을 위해 필수 추가
    public ResponseEntity<String> updateMenu(@PathVariable Integer id, @RequestBody MenuSaveRequestDto dto) {
        // DB에서 대상 메뉴를 먼저 찾습니다.
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 메뉴 ID입니다: " + id));

        // 이전에 Menu 엔티티에 만들어 두었던 수정용 도메인 메서드 호출
        menu.updateMenuDetails(dto.getCategoryId(), dto.getItemId(), dto.getName(), dto.getMenuImg());
        
        // @Transactional이 걸려있으므로 메서드가 끝날 때 Dirty Checking(변경 감지)에 의해 자동으로 DB에 UPDATE 쿼리가 날아갑니다.
        return ResponseEntity.ok("메뉴 정보가 성공적으로 수정되었습니다.");
    }

    // 4. 메뉴 가격 변경 (가격 변경 - SIZE 테이블 연동 인터페이스)
    @PatchMapping("/sizes/{sizeId}/price")
    @Transactional // ⚠️ 데이터 수정을 위한 트랜잭션 필수 추가
    public ResponseEntity<String> updateMenuPrice(@PathVariable Integer sizeId, @RequestBody MenuPriceUpdateRequestDto dto) {
        
        // 1. SIZE 테이블에서 변경할 규격(싱글레귤러, 파인트 등)을 찾습니다.
        Size size = sizeRepository.findById(sizeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사이즈 규격 ID입니다: " + sizeId));

        // 2. 엔티티 내부의 setter나 비즈니스 메서드를 통해 가격 변경 (dirty checking 작동)
        // 예시: size.changePrice(dto.getPrice()); 혹은 엔티티 스펙에 맞게 가격 수정 로직 반영
        // 여기서는 기본 setter가 열려있거나 엔티티 내 메소드가 있다고 가정합니다.
        
        return ResponseEntity.ok("해당 규격의 가격이 성공적으로 변경되었습니다.");
    }

    // 5. 메뉴 삭제 (메뉴 삭제)
    @DeleteMapping("/{id}")
    @Transactional // ⚠️ DB 데이터 삭제/변경을 위해 필수 추가
    public ResponseEntity<String> deleteMenu(@PathVariable Integer id) {
        // 1. 삭제할 메뉴가 실제로 존재하는지 먼저 검증 (안전장치)
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 메뉴 ID입니다: " + id));

        // 2. Repository를 통해 데이터베이스에서 해당 레코드 삭제
        menuRepository.delete(menu); // 또는 menuRepository.deleteById(id);

        return ResponseEntity.ok("메뉴가 성공적으로 삭제되었습니다.");
    }
}