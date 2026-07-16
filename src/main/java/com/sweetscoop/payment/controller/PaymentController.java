package com.sweetscoop.payment.controller;

import com.sweetscoop.payment.dto.PaymentRequestDTO;
import com.sweetscoop.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor   // Lombok이 final이 붙은 필드의 생성자를 자동으로 만들어 줌
public class PaymentController {

    // @Autowired 대신 final + @RequiredArgsConstructor 조합을 쓰는 것이 요즘 트렌드이자 정석입니다.
    private final PaymentService paymentService;

    @PostMapping("/toss-confirm")
    public ResponseEntity<Map<String, Object>> approvePayment(@RequestBody PaymentRequestDTO requestDTO) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> receiptData = paymentService.processTossPayment(requestDTO);
            response.put("success", true);
            response.put("message", "결제가 완료되었습니다.");
            response.put("receipt", receiptData);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            // 💡 에러 로그를 콘솔에 출력 (이걸 봐야 원인을 압니다!)
            e.printStackTrace(); 
            
            response.put("success", false);
            response.put("message", "에러 발생: " + e.getMessage()); // 프론트에서도 볼 수 있게 메시지 추가
            return ResponseEntity.badRequest().body(response);
        }
    }
}