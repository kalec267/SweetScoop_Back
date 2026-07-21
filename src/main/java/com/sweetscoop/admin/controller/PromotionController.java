package com.sweetscoop.admin.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sweetscoop.admin.dto.request.PromotionRequest;
import com.sweetscoop.admin.dto.response.PromotionResponse;
import com.sweetscoop.admin.service.PromotionService;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/promotion")
@RequiredArgsConstructor
public class PromotionController {


    private final PromotionService promotionService;


    // 등록
    @PostMapping
    public ResponseEntity<Void> save(
            @RequestBody PromotionRequest request
    ) {

        promotionService.save(request);

        return ResponseEntity.ok().build();

    }


    // 전체 조회
    @GetMapping
    public ResponseEntity<List<PromotionResponse>> findAll() {

        return ResponseEntity.ok(
                promotionService.findAll()
        );

    }


    // 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<PromotionResponse> findById(
            @PathVariable Integer id
    ) {

        return ResponseEntity.ok(
                promotionService.findById(id)
        );

    }


    // 수정
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(
            @PathVariable Integer id,
            @RequestBody PromotionRequest request
    ) {

        promotionService.update(id, request);

        return ResponseEntity.ok().build();

    }


    // 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Integer id
    ) {

        promotionService.delete(id);

        return ResponseEntity.ok().build();

    }

}