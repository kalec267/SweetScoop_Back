package com.sweetscoop.firebase; // 패키지명은 프로젝트에 맞게 확인해주세요

import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class FirebaseService {

    public void sendOrderToBranch(Integer branchId, String orderId, Map<String, Object> payload) {
        try {
            // 💡 핵심: Realtime Database가 아니라 Cloud Firestore의 "orders" 데이터베이스를 호출합니다.
            Firestore db = FirestoreClient.getFirestore("orders");

            // 최상위 'orders' 컬렉션에 주문 데이터 추가
            db.collection("orders").add(payload);

            System.out.println("🔥 [FirebaseService] Firestore 'orders' DB로 주문 전송 성공!");

        } catch (Exception e) {
            System.err.println(">>> [FirebaseService] Firebase 전송 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
}