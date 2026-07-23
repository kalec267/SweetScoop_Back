package com.sweetscoop.payment.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sweetscoop.admin.entity.Promotion;
import com.sweetscoop.payment.dto.MemberBenefitResponseDTO;
import com.sweetscoop.payment.dto.PaymentCalculationRequestDTO;
import com.sweetscoop.payment.dto.PaymentCalculationResponseDTO;
import com.sweetscoop.payment.dto.PaymentRequestDTO;
import com.sweetscoop.payment.repository.PaymentMapper;
import com.sweetscoop.payment.service.PaymentCalculationService;
import com.sweetscoop.payment.service.PaymentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    private final PaymentCalculationService
            paymentCalculationService;
    
    private final PaymentMapper payment;

    /**
     * 전화번호로 회원 포인트와 쿠폰 조회
     *
     * GET /api/payments/member-benefits
     *     ?phoneNumber=01012345678
     */
    @GetMapping("/member-benefits")
    public ResponseEntity<MemberBenefitResponseDTO>
            getMemberBenefits(
                    @RequestParam String phoneNumber
            ) {

        return ResponseEntity.ok(
                paymentCalculationService
                        .getMemberBenefits(phoneNumber)
        );
    }
    
    @GetMapping
    public ResponseEntity<List<Promotion>> getActivePromotions() {
        // DB의 모든 프로모션 목록 또는 진행 중인 목록 리턴
        List<Promotion> promotions = payment.findAll();
        return ResponseEntity.ok(promotions);
    }

    /**
     * 쿠폰과 포인트 적용 금액 계산
     */
    @PostMapping("/calculate")
    public ResponseEntity<PaymentCalculationResponseDTO>
            calculatePayment(
                    @RequestBody
                    PaymentCalculationRequestDTO request
            ) {

        return ResponseEntity.ok(
                paymentCalculationService
                        .calculate(request)
        );
    }

    /**
     * Toss 결제 승인
     */
    @PostMapping("/toss-confirm")
    public ResponseEntity<Map<String, Object>>
            approvePayment(
                    @RequestBody
                    PaymentRequestDTO requestDTO
            ) {

        Map<String, Object> response =
                new HashMap<>();

        try {
            Map<String, Object> receiptData =
                    paymentService.processTossPayment(
                            requestDTO
                    );

            response.put("success", true);
            response.put(
                    "message",
                    "결제가 완료되었습니다."
            );
            response.put("receipt", receiptData);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();

            response.put("success", false);
            response.put(
                    "message",
                    e.getMessage()
            );

            return ResponseEntity
                    .badRequest()
                    .body(response);
        }
    }
}