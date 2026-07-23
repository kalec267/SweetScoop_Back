package com.sweetscoop.admin.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sweetscoop.admin.dto.request.CSRequest;
import com.sweetscoop.admin.dto.response.CSResponse;
import com.sweetscoop.admin.entity.BranchManager;
import com.sweetscoop.admin.entity.CS;
import com.sweetscoop.admin.entity.HqManager;
import com.sweetscoop.admin.repository.BranchManagerRepository;
import com.sweetscoop.admin.repository.CSRepository;
import com.sweetscoop.admin.repository.HqManagerRepository;
import com.sweetscoop.admin.service.CSService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/cs")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class CSController {

    private final CSService csService;
    private final BranchManagerRepository branchManagerRepository;
    private final HqManagerRepository hqManagerRepository;
    private final CSRepository csRepository;
    
    // 전체 조회
    @GetMapping
    public List<CSResponse> findAll() {
        return csService.findAll();
    }

    // 상세 조회
    @GetMapping("/{id}")
    public CS findById(@PathVariable Integer id) {
        return csService.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글이 없습니다."));
    }

    // 등록
    @PreAuthorize("hasRole('BRANCH')")
    @PostMapping
    public CS save(@RequestBody CSRequest request) {

        CS.CSBuilder builder = CS.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .createdAt(LocalDateTime.now());


        if(request.getManagerId() != null) {

            BranchManager manager =
                    branchManagerRepository.findByLoginId(request.getManagerId())
                    .orElseThrow(() ->
                            new RuntimeException("지점장이 없습니다.")
                    );

            builder.manager(manager);

        } 
        else if(request.getHqManagerId() != null) {

            HqManager hqManager =
                    hqManagerRepository.findByLoginId(request.getHqManagerId())
                    .orElseThrow(() ->
                            new RuntimeException("본사 관리자가 없습니다.")
                    );

            builder.hqManager(hqManager);

        }
        else {
            throw new RuntimeException("작성자가 없습니다.");
        }


        return csRepository.save(builder.build());
    }

    // 수정
    @PutMapping("/{id}")
    public CSResponse update(
            @PathVariable Integer id,
            @RequestBody CSRequest request
    ) {

        return csService.update(id, request);
    }

    // 삭제
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        csService.delete(id);
    }
    //답변
    @PreAuthorize("hasRole('HQ')")
    @PutMapping("/{id}/answer")
    public CS answer(
            @PathVariable Integer id,
            @RequestBody CSRequest request
    ){

        CS cs = csRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("문의가 없습니다.")
                );


        HqManager hqManager =
                hqManagerRepository.findByLoginId(request.getHqManagerId())
                .orElseThrow(() ->
                        new RuntimeException("본사 관리자가 없습니다.")
                );


        cs.answer(
                request.getAnswer(),
                hqManager
        );


        return csRepository.save(cs);
    }

}