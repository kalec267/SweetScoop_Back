package com.sweetscoop.scheduler;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderResetScheduler {

    /**
     * 매일 00시 00분 00초에 실행
     * - DB(ORDERS)는 유지하고, 실시간 화면용 Firebase 문서만 삭제하여 대시보드를 리셋합니다.
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void resetDailyOrders() {
        Firestore db = FirestoreClient.getFirestore();
        try {
            ApiFuture<QuerySnapshot> future = db.collection("orders").get();
            // 1. future.get()으로 QuerySnapshot을 받아옵니다.
            QuerySnapshot querySnapshot = future.get();

            // 2. querySnapshot에서 .getDocuments()를 호출하여 List를 추출합니다.
            List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();

            for (QueryDocumentSnapshot document : documents) {
                document.getReference().delete();
            }

            System.out.println("[자정 초기화 완료] Firebase 실시간 주문 대시보드가 초기화되었습니다.");
            
        } catch (Exception e) {
            System.err.println("[오류] 자정 초기화 실패: " + e.getMessage());
        }
    }
}
