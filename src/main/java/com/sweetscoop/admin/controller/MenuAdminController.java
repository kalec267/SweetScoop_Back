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

	// 1. 메뉴 전체 조회 (price 매핑 추가)
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
	                    .categoryId(m.getCategoryId())
	                    .itemId(m.getItemId())
	                    .price(m.getPrice()) // 👈 💡 [핵심] price 필드 매핑 추가!
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

    // 4. 메뉴 가격 변경 (가격 변경 - MENU 테이블 연동 인터페이스)
    @PatchMapping("/{id}/price") // 👈 '/sizes/{sizeId}/price' 에서 '/{id}/price' 로 수정!
    @Transactional
    public ResponseEntity<String> updateMenuPrice(
            @PathVariable Integer id, // 👈 sizeId 대신 id(menuId)로 받음
            @RequestBody MenuPriceUpdateRequestDto dto) {
        
        // 1. MENU 테이블에서 해당 메뉴 조회
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 메뉴 ID입니다: " + id));

        // 2. 가격 변경 (Dirty Checking에 의해 DB 자동 UPDATE)
        menu.updatePrice(dto.getPrice());
        
        return ResponseEntity.ok("메뉴 가격이 성공적으로 변경되었습니다.");
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