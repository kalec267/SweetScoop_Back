package com.sweetscoop.firebase;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult; // 💡 임포트 추가 필요
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class FirebaseService {

    /**
     * PaymentService에서 호출하는 메서드 규격에 맞춘 실시간 주문 전송 메서드
     */
    public String sendOrderToBranch(Integer branchId, String orderId, Map<String, Object> orderData) {
        try {
            Firestore db = FirestoreClient.getFirestore();
            
            // 데이터에 지점 ID도 함께 포함시켜 줍니다.
            orderData.put("branchId", branchId);
            
            // 💡 ApiFuture<WriteResult>로 타입 수정 (또는 var 사용 가능)
            ApiFuture<WriteResult> future = db.collection("orders").document(orderId).set(orderData);
            future.get(); // 저장이 완료될 때까지 안전하게 대기
            
            System.out.println("🔥 [백엔드] 지점(" + branchId + ") 주문 Firestore 전송 성공! 주문 ID: " + orderId);
            return orderId;
            
        } catch (Exception e) {
            System.err.println("❌ [백엔드] Firebase 전송 실패 상세 에러: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public String saveOrder(Map<String, Object> orderData) {
        try {
            Firestore db = FirestoreClient.getFirestore();
            var future = db.collection("orders").add(orderData);
            String docId = future.get().getId();
            System.out.println("🔥 [백엔드] 주문 내역 Firestore 저장 성공! 문서 ID: " + docId);
            return docId;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}