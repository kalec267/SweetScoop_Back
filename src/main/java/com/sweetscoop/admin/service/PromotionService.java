package com.sweetscoop.admin.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sweetscoop.admin.dto.request.PromotionRequest;
import com.sweetscoop.admin.dto.response.PromotionResponse;
import com.sweetscoop.admin.entity.Promotion;
import com.sweetscoop.admin.repository.PromotionRepository;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
@Transactional
public class PromotionService {


    private final PromotionRepository promotionRepository;


    // 등록
    public void save(PromotionRequest request) {

        Promotion promotion = Promotion.builder()
                .eventName(request.getEventName())
                .eventContent(request.getEventContent())
                .imageCode(request.getImageCode())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .build();


        promotionRepository.save(promotion);

    }


    // 전체 조회
    @Transactional(readOnly = true)
    public List<PromotionResponse> findAll() {

        return promotionRepository.findAll()
                .stream()
                .map(PromotionResponse::new)
                .toList();

    }


    // 단건 조회
    @Transactional(readOnly = true)
    public PromotionResponse findById(Integer id) {

        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("이벤트 없음"));


        return new PromotionResponse(promotion);

    }


    // 수정
    public void update(
            Integer id,
            PromotionRequest request
    ) {

        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("이벤트 없음"));


        promotion.update(
                request.getEventName(),
                request.getEventContent(),
                request.getImageCode(),
                request.getStartDate(),
                request.getEndDate(),
                request.getStartTime(),
                request.getEndTime()
        );

    }


    // 삭제
    public void delete(Integer id) {

        promotionRepository.deleteById(id);

    }

}