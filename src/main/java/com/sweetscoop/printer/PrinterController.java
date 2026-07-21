package com.sweetscoop.printer;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/printer")
public class PrinterController {

//    private final String PRINTER_URL = "http://172.16.15.97:7777/receipt";
    private final String PRINTER_URL = "http://host.docker.internal:8888/receipt";
    

    @PostMapping("/print")
    public ResponseEntity<String> sendToPrinter(@RequestBody Map<String, Object> orderData) {
        // 1. 프론트에서 받은 전체 데이터 확인
        System.out.println(">>> [PrinterController] 수신된 데이터: " + orderData);

        String orderNo = String.valueOf(orderData.getOrDefault("orderNo", ""));
        String orderItem = String.valueOf(orderData.getOrDefault("orderItem", ""));
        String price = String.valueOf(orderData.getOrDefault("price", ""));
        String orderDate = String.valueOf(orderData.getOrDefault("orderDate", ""));

        Map<String, String> printerPayload = new HashMap<>();
        printerPayload.put("orderNo", orderNo);
        printerPayload.put("orderItem", orderItem);
        printerPayload.put("price", price);
        printerPayload.put("orderDate", orderDate);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(printerPayload, headers);

        try {
            // 2. 프린터 서버로 보내기 전 데이터 확인
            System.out.println(">>> [PrinterController] 프린터 서버 전송 URL: " + PRINTER_URL);
            System.out.println(">>> [PrinterController] 프린터 서버 전송 JSON: " + printerPayload);

            ResponseEntity<String> response = restTemplate.postForEntity(PRINTER_URL, entity, String.class);
            
            // 3. 프린터 서버로부터 받은 응답 확인
            System.out.println(">>> [PrinterController] 프린터 서버 응답: " + response.getBody());
            
            return response;
        } catch (Exception e) {
            // 4. 에러 발생 시 상세 정보 출력
            System.err.println(">>> [PrinterController] 전송 중 에러 발생: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("FAIL");
        }
    }
}