package com.sweetscoop.payment.service;

import com.sweetscoop.payment.dto.PaymentRequestDTO;
import com.sweetscoop.payment.repository.PaymentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    public Map<String, Object> processTossPayment(
            PaymentRequestDTO dto) throws Exception {

        validateRequest(dto);

        System.out.println(
                "디버깅 - 결제 요청된 orderId: "
                        + dto.getOrderId()
        );

        /*
         * 1. 주문 행 잠금 및 중복 결제 방지
         */
        Map<String, Object> order =
                paymentMapper.selectOrderForUpdate(
                        dto.getOrderId()
                );

        if (order == null) {
            throw new Exception(
                    "존재하지 않는 주문 번호입니다."
            );
        }

        Object statusValue = order.get("status");

        if (
                statusValue != null
                        && "결제완료".equals(
                        String.valueOf(statusValue)
                )
        ) {
            throw new Exception(
                    "이미 완결된 주문서입니다."
            );
        }

        /*
         * 2. Toss Payments 승인 API 호출
         */
        String testSecretKey =
                "test_sk_DpexMgkW36vBRZQPEjKd3GbR5ozO:";

        String basicAuthHeader =
                Base64.getEncoder()
                        .encodeToString(
                                testSecretKey.getBytes(
                                        StandardCharsets.UTF_8
                                )
                        );

        RestTemplate restTemplate =
                new RestTemplate();

        HttpHeaders headers =
                new HttpHeaders();

        headers.setContentType(
                MediaType.APPLICATION_JSON
        );

        headers.set(
                "Authorization",
                "Basic " + basicAuthHeader
        );

        Map<String, Object> tossBody =
                new HashMap<>();

        tossBody.put(
                "paymentKey",
                dto.getPaymentKey()
        );

        tossBody.put(
                "amount",
                dto.getAmount()
        );

        tossBody.put(
                "orderId",
                dto.getTossOrderId()
        );

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(
                        tossBody,
                        headers
                );

        Map<String, Object> tossResponseBody;

        try {
            ResponseEntity<Map> response =
                    restTemplate.postForEntity(
                            "https://api.tosspayments.com/v1/payments/confirm",
                            entity,
                            Map.class
                    );

            if (
                    response.getStatusCode()
                            != HttpStatus.OK
            ) {
                throw new Exception(
                        "토스 승인 응답 상태가 정상적이지 않습니다: "
                                + response.getStatusCode()
                );
            }

            if (response.getBody() == null) {
                throw new Exception(
                        "토스 승인 응답 본문이 없습니다."
                );
            }

            tossResponseBody =
                    (Map<String, Object>)
                            response.getBody();

        } catch (Exception e) {
            throw new Exception(
                    "토스페이먼츠 승인 연동 실패: "
                            + e.getMessage(),
                    e
            );
        }

        /*
         * 3. 토스 승인 응답에서 결제수단과 카드사 추출
         */
        setPaymentMethod(
                dto,
                tossResponseBody
        );

        setCardCompany(
                dto,
                tossResponseBody
        );

        /*
         * DB NOT NULL 방어값
         */
        if (
                dto.getMethod() == null
                        || dto.getMethod().isBlank()
        ) {
            dto.setMethod("카드");
        }

        if (
                dto.getCardCompany() == null
                        || dto.getCardCompany().isBlank()
        ) {
            dto.setCardCompany(
                    "결제사 정보 없음"
            );
        }

        System.out.println(
                "디버깅 - 저장할 결제수단: "
                        + dto.getMethod()
        );

        System.out.println(
                "디버깅 - 저장할 카드사 정보: "
                        + dto.getCardCompany()
        );

        /*
         * 4. PAYMENT 저장
         */
        int payResult =
                paymentMapper.insertPayment(
                        dto,
                        dto.getPaymentKey()
                );

        if (payResult <= 0) {
            throw new Exception(
                    "PAYMENT 내역 생성 실패"
            );
        }

        /*
         * 5. 주문 상태 및 대기번호 갱신
         */
        int waitingNo =
                (int) (Math.random() * 900) + 100;

        int orderResult =
                paymentMapper.updateOrderStatus(
                        dto.getOrderId(),
                        "결제완료",
                        waitingNo
                );

        if (orderResult <= 0) {
            throw new Exception(
                    "ORDERS 상태 업데이트 실패"
            );
        }

        /*
         * 6. 영수증 데이터 반환
         */
        Map<String, Object> receiptData =
                paymentMapper.selectReceiptDetails(
                        dto.getOrderId()
                );

        if (receiptData == null) {
            receiptData = new HashMap<>();
        }

        List<Map<String, Object>> items =
                paymentMapper.selectOrderItems(
                        dto.getOrderId()
                );

        receiptData.put(
                "items",
                items
        );

        return receiptData;
    }

    private void validateRequest(
            PaymentRequestDTO dto) throws Exception {

        if (dto == null) {
            throw new Exception(
                    "결제 요청 데이터가 없습니다."
            );
        }

        if (
                dto.getOrderId() == null
                        || dto.getOrderId().isBlank()
        ) {
            throw new Exception(
                    "DB 주문번호가 없습니다."
            );
        }

        if (
                dto.getTossOrderId() == null
                        || dto.getTossOrderId().isBlank()
        ) {
            throw new Exception(
                    "토스 주문번호가 없습니다."
            );
        }

        if (
                dto.getPaymentKey() == null
                        || dto.getPaymentKey().isBlank()
        ) {
            throw new Exception(
                    "토스 결제키가 없습니다."
            );
        }

        if (dto.getAmount() <= 0) {
            throw new Exception(
                    "결제 금액이 올바르지 않습니다."
            );
        }
    }

    private void setPaymentMethod(
            PaymentRequestDTO dto,
            Map<String, Object> tossResponseBody) {

        Object methodValue =
                tossResponseBody.get("method");

        if (methodValue != null) {
            String method =
                    String.valueOf(methodValue);

            if (!method.isBlank()) {
                dto.setMethod(method);
                return;
            }
        }

        /*
         * 프론트에서 method가 넘어왔다면 유지하고,
         * 없을 때만 기본값을 적용한다.
         */
        if (
                dto.getMethod() == null
                        || dto.getMethod().isBlank()
        ) {
            dto.setMethod("카드");
        }
    }

    private void setCardCompany(
            PaymentRequestDTO dto,
            Map<String, Object> tossResponseBody) {

        Object cardValue =
                tossResponseBody.get("card");

        if (
                cardValue
                        instanceof Map<?, ?> cardMap
        ) {
            Object companyValue =
                    cardMap.get("company");

            if (companyValue != null) {
                String company =
                        String.valueOf(
                                companyValue
                        );

                if (!company.isBlank()) {
                    dto.setCardCompany(
                            company
                    );
                    return;
                }
            }
        }

        Object easyPayValue =
                tossResponseBody.get("easyPay");

        if (
                easyPayValue
                        instanceof Map<?, ?> easyPayMap
        ) {
            Object providerValue =
                    easyPayMap.get("provider");

            if (providerValue != null) {
                String provider =
                        String.valueOf(
                                providerValue
                        );

                if (!provider.isBlank()) {
                    dto.setCardCompany(
                            provider
                    );
                    return;
                }
            }
        }

        if (
                dto.getCardCompany() == null
                        || dto.getCardCompany().isBlank()
        ) {
            dto.setCardCompany(
                    "토스페이먼츠"
            );
        }
    }
}