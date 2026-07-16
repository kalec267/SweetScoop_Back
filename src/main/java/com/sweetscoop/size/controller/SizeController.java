package com.sweetscoop.size.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import com.sweetscoop.size.dto.SizeDTO;
import com.sweetscoop.size.service.SizeService;

@RestController
@RequestMapping("/api/size")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class SizeController {

    private final SizeService sizeService;

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<SizeDTO>> getSizeList(
            @PathVariable Integer categoryId){

        return ResponseEntity.ok(
                sizeService.getSizeList(categoryId));

    }

    @GetMapping("/{id}")
    public ResponseEntity<SizeDTO> getSize(
            @PathVariable Integer id){

        return ResponseEntity.ok(
                sizeService.getSize(id));

    }

}