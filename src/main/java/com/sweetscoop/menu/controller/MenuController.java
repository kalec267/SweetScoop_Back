package com.sweetscoop.menu.controller;


import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import com.sweetscoop.menu.dto.MenuDTO;
import com.sweetscoop.menu.service.MenuService;


@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class MenuController {


    private final MenuService menuService;


    // 전체 메뉴 조회
    @GetMapping
    public ResponseEntity<List<MenuDTO>> getMenuList(){

        return ResponseEntity.ok(
                menuService.getMenuList()
        );
    }


    // 메뉴 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<MenuDTO> getMenu(
            @PathVariable Integer id
    ){

        return ResponseEntity.ok(
                menuService.getMenu(id)
        );
    }


    // 카테고리 조회
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<MenuDTO>> getCategoryMenu(
            @PathVariable Integer categoryId
    ){

        return ResponseEntity.ok(
                menuService.getMenuByCategory(categoryId)
        );
    }

}