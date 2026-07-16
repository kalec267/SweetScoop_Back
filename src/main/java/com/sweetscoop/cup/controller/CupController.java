package com.sweetscoop.cup.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import com.sweetscoop.cup.dto.CupDTO;
import com.sweetscoop.cup.service.CupService;

@RestController
@RequestMapping("/api/cup")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class CupController {

    private final CupService cupService;

    @GetMapping
    public ResponseEntity<List<CupDTO>> getCupList() {

        return ResponseEntity.ok(
                cupService.getCupList());

    }
}