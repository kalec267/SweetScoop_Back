package com.sweetscoop.sales.service;

import com.sweetscoop.sales.repository.SalesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SalesService {

    private final SalesRepository salesRepository;

    public Map<String, Object> getDashboardData(Integer branchId, String filter) {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start;

        int daysPeriod = 1;
        switch (filter) {
            case "today":
                start = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
                daysPeriod = 1;
                break;
            case "7days":
                start = LocalDateTime.now().minusDays(7);
                daysPeriod = 7;
                break;
            case "30days":
                start = LocalDateTime.now().minusDays(30);
                daysPeriod = 30;
                break;
            default:
                start = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        }

        Map<String, Object> result = new HashMap<>();

        // 1. 상단 카드 정보
        List<Object[]> summaryList = (List<Object[]>) salesRepository.getSummaryStats(branchId, start, end);
        Long totalSales = 0L;
        Long totalCount = 0L;
        if (summaryList != null && !summaryList.isEmpty()) {
            Object[] summary = summaryList.get(0);
            totalSales = (summary[0] != null) ? ((Number) summary[0]).longValue() : 0L;
            totalCount = (summary[1] != null) ? ((Number) summary[1]).longValue() : 0L;
        }
        result.put("cumulativeSales", totalSales);
        result.put("averageReceipt", totalCount > 0 ? totalSales / totalCount : 0L);

        // 2. [차트 1] 상단용: 기간별 전체 매출 통계 추이 수정
        List<Map<String, Object>> periodTrends = new ArrayList<>();

        if ("today".equals(filter)) {
            List<Object[]> hourlyRawForPeriod = salesRepository.getHourlySales(branchId, start, end);
            
            Map<Integer, Long> todayMap = new HashMap<>();
            // 반복문 시작을 10으로, 끝을 23으로 변경 (오전 10시 ~ 오후 11시)
            for (int i = 10; i <= 23; i++) todayMap.put(i, 0L);
            for (Object[] row : hourlyRawForPeriod) {
                if (row[0] != null) {
                    todayMap.put(((Number) row[0]).intValue(), ((Number) row[1]).longValue());
                }
            }
            // 오전 10시부터 오후 11시까지만 순서대로 세팅
            for (int i = 10; i <= 23; i++) {
                Map<String, Object> point = new HashMap<>();
                point.put("axisLabel", i + "시");
                point.put("amount", todayMap.get(i));
                periodTrends.add(point);
            }
        } else {
            // 7일, 30일일 때는 기존처럼 날짜별(일별) 집계 유지
            List<Object[]> dailyRaw = salesRepository.getDailySales(branchId, start, end);
            for (Object[] row : dailyRaw) {
                Map<String, Object> point = new HashMap<>();
                point.put("axisLabel", row[0].toString());
                point.put("amount", row[1]);
                periodTrends.add(point);
            }
        }
        result.put("periodTrends", periodTrends);

        // 3. [차트 2] 하단용: 일/주/월 시간대별 분석 (0시~23시 고정)
        List<Object[]> hourlyRaw = salesRepository.getHourlySales(branchId, start, end);
        Map<Integer, Long> hourlyMap = new HashMap<>();
        for (int i = 10; i < 23; i++) hourlyMap.put(i, 0L);
        for (Object[] row : hourlyRaw) {
            if (row[0] != null) {
                hourlyMap.put(((Number) row[0]).intValue(), ((Number) row[1]).longValue());
            }
        }

        List<Map<String, Object>> hourlyChart = new ArrayList<>();
        for (int i = 10; i < 23; i++) {
            Map<String, Object> point = new HashMap<>();
            point.put("axisLabel", i + "시");
            long amount = hourlyMap.get(i);
            // 7일이나 30일이면 보기 편하게 시간대별 '평균' 값으로 계산
            point.put("amount", filter.equals("today") ? amount : amount / daysPeriod);
            hourlyChart.add(point);
        }
        result.put("hourlyTrends", hourlyChart);

        // 4. 인기 메뉴 TOP 5
        List<Object[]> rankingRaw = salesRepository.getTopMenuRanking(branchId, start, end);
        List<Map<String, Object>> topFlavors = new ArrayList<>();
        long totalFlavorsCount = rankingRaw.stream().mapToLong(row -> (Long) row[1]).sum();
        int limit = 0;
        for (Object[] row : rankingRaw) {
            if (limit >= 5) break;
            Map<String, Object> flavor = new HashMap<>();
            flavor.put("name", row[0]);
            long count = (Long) row[1];
            flavor.put("share", totalFlavorsCount > 0 ? Math.round((double) count / totalFlavorsCount * 100) : 0);
            topFlavors.add(flavor);
            limit++;
        }
        result.put("topFlavors", topFlavors);

        // 5. 지점별 매출 랭킹 가공
        List<Object[]> branchRaw = salesRepository.getBranchSalesRanking(start, end);
        List<Map<String, Object>> branchRanking = new ArrayList<>();
        for (Object[] row : branchRaw) {
            Map<String, Object> branchData = new HashMap<>();
            branchData.put("branchName", row[0].toString()); 
            branchData.put("totalAmount", row[1]);
            branchRanking.add(branchData);
        }
        result.put("branchRanking", branchRanking);

     // 6. 주문 유형별 통계 가공
        List<Object[]> orderTypeRaw = salesRepository.getOrderTypeStats(branchId, start, end);
        List<Map<String, Object>> orderTypes = new ArrayList<>();
        for (Object[] row : orderTypeRaw) {
            Map<String, Object> map = new HashMap<>();
            map.put("label", row[0]);
            map.put("value", row[1]);
            orderTypes.add(map);
        }
        result.put("orderTypes", orderTypes);

        // 7. 결제 수단별 통계 가공
        List<Object[]> paymentMethodRaw = salesRepository.getPaymentMethodStats(branchId, start, end);
        List<Map<String, Object>> paymentMethods = new ArrayList<>();
        for (Object[] row : paymentMethodRaw) {
            Map<String, Object> map = new HashMap<>();
            map.put("label", row[0]);
            map.put("value", row[1]);
            paymentMethods.add(map);
        }
        result.put("paymentMethods", paymentMethods);
        
        return result;
    }
}