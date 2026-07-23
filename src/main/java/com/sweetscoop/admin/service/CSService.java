package com.sweetscoop.admin.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sweetscoop.admin.dto.request.CSRequest;
import com.sweetscoop.admin.dto.response.CSResponse;
import com.sweetscoop.admin.entity.BranchManager;
import com.sweetscoop.admin.entity.CS;
import com.sweetscoop.admin.entity.HqManager;
import com.sweetscoop.admin.repository.BranchManagerRepository;
import com.sweetscoop.admin.repository.CSRepository;
import com.sweetscoop.admin.repository.HqManagerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CSService {

    private final CSRepository csRepository;
    private final BranchManagerRepository branchManagerRepository;
    private final HqManagerRepository hqManagerRepository;

    // 전체 조회
    public List<CSResponse> findAll() {

        return csRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(cs -> new CSResponse(
                        cs.getId(),
                        cs.getTitle(),
                        cs.getContent(),
                        cs.getCreatedAt(),
                        cs.getManager() != null
                        ? cs.getManager().getName()
                        : null,
                        cs.getHqManager() != null
                            ? cs.getHqManager().getName()
                            : null, cs.getAnswer(),
                            cs.getAnsweredAt()
                ))
                .toList();
    }

    // 상세 조회
    public Optional<CS> findById(Integer id) {
        return csRepository.findById(id);
    }

    // 등록
    public CS save(CSRequest request) {

        CS.CSBuilder builder = CS.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .createdAt(LocalDateTime.now());


        if(request.getManagerId() != null){

            BranchManager manager =
                branchManagerRepository.findByLoginId(request.getManagerId())
                .orElseThrow(() ->
                    new RuntimeException("지점장이 없습니다.")
                );

            builder.manager(manager);
        }


        if(request.getHqManagerId() != null){

            HqManager hqManager =
            	hqManagerRepository.findByLoginId(request.getHqManagerId())
                .orElseThrow(() ->
                    new RuntimeException("본사 관리자가 없습니다.")
                );

            builder.hqManager(hqManager);
        }


        return csRepository.save(builder.build());
    }

    // 수정
    @Transactional
    public CSResponse update(Integer id, CSRequest request){

        CS cs = csRepository.findById(id)
                .orElseThrow();
        
        if(cs.getAnswer() != null){
            throw new RuntimeException("답변 완료된 문의는 수정할 수 없습니다.");
        }

        cs.update(
            request.getTitle(),
            request.getContent()
        );

        return new CSResponse(
        	    cs.getId(),
        	    cs.getTitle(),
        	    cs.getContent(),
        	    cs.getCreatedAt(),
        	    cs.getManager() != null
        	        ? cs.getManager().getName()
        	        : null,
        	    cs.getHqManager() != null
        	        ? cs.getHqManager().getName()
        	        : null,
        	    cs.getAnswer(),
        	    cs.getAnsweredAt()
        	);
    }

    // 삭제
    public void delete(Integer id) {
        csRepository.deleteById(id);
    }
    // 답변
    @Transactional
    public CSResponse answer(
            Integer id,
            String answer,
            String hqManagerId
    ){

        CS cs = csRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("문의가 없습니다.")
                );


        HqManager hqManager =
                hqManagerRepository.findByLoginId(hqManagerId)
                .orElseThrow(() ->
                        new RuntimeException("본사 관리자가 없습니다.")
                );


        cs.answer(answer, hqManager);


        return new CSResponse(
                cs.getId(),
                cs.getTitle(),
                cs.getContent(),
                cs.getCreatedAt(),
                cs.getManager() != null
                    ? cs.getManager().getName()
                    : null,
                cs.getHqManager() != null
                    ? cs.getHqManager().getName()
                    : null,
                cs.getAnswer(),
                cs.getAnsweredAt()
        );
    }

}