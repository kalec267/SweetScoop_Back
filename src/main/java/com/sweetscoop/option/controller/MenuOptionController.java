package com.sweetscoop.option.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.sweetscoop.option.dto.MenuOptionDTO;
import com.sweetscoop.option.service.MenuOptionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/menu-options")
@RequiredArgsConstructor
public class MenuOptionController {

    private final MenuOptionService menuOptionService;

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<MenuOptionDTO>>
            getOptionsByCategory(
                    @PathVariable Integer categoryId) {

        return ResponseEntity.ok(
                menuOptionService.getOptionsByCategory(
                        categoryId
                )
        );
    }
}