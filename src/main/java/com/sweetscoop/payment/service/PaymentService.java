package com.sweetscoop.payment.service;

import com.sweetscoop.payment.dto.PaymentRequestDTO;
import com.sweetscoop.payment.repository.PaymentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentMapper paymentMapper;

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> processTossPayment(PaymentRequestDTO dto) throws Exception {
        
    	// 💡 로그 추가: 프론트에서 넘어온 orderId가 무엇인지 정확히 확인
        System.out.println("디버깅 - 결제 요청된 orderId: " + dto.getOrderId());
    	
        // 1. ORDERS 레코드에 비관적 락(FOR UPDATE)을 걸어 다중 결제 요청 및 동시성 차단
        Map<String, Object> order = paymentMapper.selectOrderForUpdate(dto.getOrderId());
        if (order == null) throw new Exception("존재하지 않는 주문 번호입니다.");
        if ("결제완료".equals(order.get("status"))) throw new Exception("이미 완결된 주문서입니다.");

        // 2. 🚀 토스페이먼츠 공식 승인 서버와 RestTemplate 통신 승인 검증 진행
        // 공용 테스트 시크릿 키 뒤에 콜론(:) 문자를 합쳐서 Base64 인코딩 표준 사양 준수
        String testSecretKey = "test_sk_DpexMgkW36vBRZQPEjKd3GbR5ozO:";
        String basicAuthHeader = Base64.getEncoder().encodeToString(testSecretKey.getBytes(StandardCharsets.UTF_8));

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic " + basicAuthHeader);

        Map<String, Object> tossBody = new HashMap<>();
        tossBody.put("paymentKey", dto.getPaymentKey());
        tossBody.put("amount", dto.getAmount());
        tossBody.put("orderId", dto.getTossOrderId());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(tossBody, headers);
        
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://api.tosspayments.com/v1/payments/confirm", entity, Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> resBody = response.getBody();
                if (resBody != null && resBody.containsKey("card")) {
                    Map<String, Object> cardMap = (Map<String, Object>) resBody.get("card");
                    dto.setCardCompany((String) cardMap.get("company"));
                } else {
                    dto.setCardCompany("토스지정 결제사");
                }
            }
        } catch (Exception e) {
            throw new Exception("토스페이먼츠 외연 승인 연동 실패: " + e.getMessage());
        }

        // 3. PAYMENT 테이블 데이터 인서트적재
        if (dto.getCardCompany() == null || dto.getCardCompany().isEmpty()) {
            dto.setCardCompany("결제사 정보 없음"); // 기본값 설정
        }
        System.out.println("디버깅 - 저장할 카드사 정보: " + dto.getCardCompany());
        int payResult = paymentMapper.insertPayment(dto, dto.getPaymentKey());
        if (payResult <= 0) throw new Exception("PAYMENT 내역 생성 실패");

        // 4. ORDERS 테이블 상태 '결제완료' 변경 및 대기번호 교부 (100~999번 대 생성)
        int waitingNo = (int) (Math.random() * 900) + 1;
        int orderResult = paymentMapper.updateOrderStatus(dto.getOrderId(), "결제완료", waitingNo);
        if (orderResult <= 0) throw new Exception("ORDERS 상태 업데이트 실패");

        // 5. 프론트엔드가 하드웨어 영수증을 출력하도록 세부 데이터 리턴
        Map<String, Object> receiptData = paymentMapper.selectReceiptDetails(dto.getOrderId());
        List<Map<String, Object>> items = paymentMapper.selectOrderItems(dto.getOrderId());

        receiptData.put("items", items); // receiptData 맵에 메뉴 리스트를 추가!
        return receiptData;
    }
}