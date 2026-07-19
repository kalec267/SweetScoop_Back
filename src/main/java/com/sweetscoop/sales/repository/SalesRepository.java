package com.sweetscoop.sales.repository;

import com.sweetscoop.sales.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface SalesRepository extends JpaRepository<Orders, Integer> {

    // 1. 기간 내 총 매출액 및 총 판매 건수 (branchId가 0이면 전체 통합 매출 조회)
    @Query(value = "SELECT SUM(o.total_price), COUNT(o.id) FROM ORDERS o " +
                   "WHERE (:branchId = 0 OR o.branch_id = :branchId) AND o.status = '결제완료' " +
                   "AND o.created_at BETWEEN :start AND :end", 
           nativeQuery = true)
    List<Object[]> getSummaryStats(@Param("branchId") Integer branchId, 
                                   @Param("start") LocalDateTime start, 
                                   @Param("end") LocalDateTime end);

    // 2. 시간대별 실시간 매출 트렌드 (branchId가 0이면 전체 통합 시간대 조회)
    @Query(value = "SELECT HOUR(o.created_at) AS hr, SUM(o.total_price) FROM ORDERS o " +
                   "WHERE (:branchId = 0 OR o.branch_id = :branchId) AND o.status = '결제완료' " +
                   "AND o.created_at BETWEEN :start AND :end " +
                   "GROUP BY HOUR(o.created_at) " +
                   "ORDER BY hr ASC", 
           nativeQuery = true)
    List<Object[]> getHourlySales(@Param("branchId") Integer branchId, 
                                  @Param("start") LocalDateTime start, 
                                  @Param("end") LocalDateTime end);

    // 3. 주간/월간용 날짜별 매출 트렌드 (branchId가 0이면 전체 통합 날짜별 조회)
    @Query(value = "SELECT DATE_FORMAT(o.created_at, '%m-%d') AS dt, SUM(o.total_price) FROM ORDERS o " +
                   "WHERE (:branchId = 0 OR o.branch_id = :branchId) AND o.status = '결제완료' " +
                   "AND o.created_at BETWEEN :start AND :end " +
                   "GROUP BY DATE_FORMAT(o.created_at, '%m-%d') " +
                   "ORDER BY dt ASC", 
           nativeQuery = true)
    List<Object[]> getDailySales(@Param("branchId") Integer branchId, 
                                 @Param("start") LocalDateTime start, 
                                 @Param("end") LocalDateTime end);

    // 4. 골든 플레이버 TOP 5 (branchId가 0이면 전체 통합 메뉴 조회)
    @Query(value = "SELECT m.name, COUNT(oim.id) FROM ORDERS o " +
                   "JOIN ORDERITEM oi ON o.id = oi.order_id " +
                   "JOIN ORDERITEMMENU oim ON oi.id = oim.order_item_id " +
                   "JOIN MENU m ON oim.menu_id = m.id " +
                   "WHERE (:branchId = 0 OR o.branch_id = :branchId) AND o.status = '결제완료' " +
                   "AND o.created_at BETWEEN :start AND :end " +
                   "GROUP BY m.id " +
                   "ORDER BY COUNT(oim.id) DESC", 
           nativeQuery = true)
    List<Object[]> getTopMenuRanking(@Param("branchId") Integer branchId, 
                                     @Param("start") LocalDateTime start, 
                                     @Param("end") LocalDateTime end);

 // 5. 지점별 실제 이름과 실시간 주문 금액 합산 기준 랭킹 조회
    @Query(value = "SELECT b.branch_name, SUM(o.total_price) AS branch_total " +
                   "FROM ORDERS o " +
                   "JOIN BRANCH b ON o.branch_id = b.id " +
                   "WHERE o.status = '결제완료' " +
                   "AND o.created_at BETWEEN :start AND :end " +
                   "GROUP BY o.branch_id, b.branch_name " +
                   "ORDER BY branch_total DESC", 
           nativeQuery = true)
    List<Object[]> getBranchSalesRanking(@Param("start") LocalDateTime start, 
                                         @Param("end") LocalDateTime end);
    
 // 6. 주문 유형별(매장/포장) 통계
    @Query(value = "SELECT o.order_type, COUNT(o.id) FROM ORDERS o " +
                   "WHERE (:branchId = 0 OR o.branch_id = :branchId) AND o.status = '결제완료' " +
                   "AND o.created_at BETWEEN :start AND :end " +
                   "GROUP BY o.order_type", 
           nativeQuery = true)
    List<Object[]> getOrderTypeStats(@Param("branchId") Integer branchId, 
                                     @Param("start") LocalDateTime start, 
                                     @Param("end") LocalDateTime end);

    // 7. 결제 수단별 통계
    @Query(value = "SELECT p.method, COUNT(p.id) FROM PAYMENT p " +
                   "JOIN ORDERS o ON p.order_id = o.id " +
                   "WHERE (:branchId = 0 OR o.branch_id = :branchId) AND o.status = '결제완료' " +
                   "AND o.created_at BETWEEN :start AND :end " +
                   "GROUP BY p.method", 
           nativeQuery = true)
    List<Object[]> getPaymentMethodStats(@Param("branchId") Integer branchId, 
                                         @Param("start") LocalDateTime start, 
                                         @Param("end") LocalDateTime end);
}