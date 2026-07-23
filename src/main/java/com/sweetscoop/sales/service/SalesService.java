package com.sweetscoop.sales.service;

import com.sweetscoop.sales.repository.SalesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SalesService {

    private final SalesRepository salesRepository;

    public Map<String, Object> getDashboardData(Integer branchId, String filter, String startDateStr, String endDateStr) {
        LocalDateTime start;
        LocalDateTime end;
        int daysPeriod = 1;

        // custom 필터(직접 선택) 및 기존 filter 분기 처리
        if ("custom".equals(filter) && startDateStr != null && !startDateStr.isBlank() && endDateStr != null && !endDateStr.isBlank()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate startLocalDate = LocalDate.parse(startDateStr, formatter);
            LocalDate endLocalDate = LocalDate.parse(endDateStr, formatter);

            start = LocalDateTime.of(startLocalDate, LocalTime.MIN);
            end = LocalDateTime.of(endLocalDate, LocalTime.MAX);

            // 일수 계산 (예: 7/1~7/1 이면 1일)
            daysPeriod = (int) ChronoUnit.DAYS.between(startLocalDate, endLocalDate) + 1;
            if (daysPeriod <= 0) daysPeriod = 1;

        } else {
            end = LocalDateTime.now();
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
                    daysPeriod = 1;
            }
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
        Long avgReceipt = totalCount > 0 ? totalSales / totalCount : 0L;

        Long totalCost = salesRepository.getTotalCostStats(branchId, start, end);
        if (totalCost == null) totalCost = 0L;

        Long netProfit = totalSales - totalCost; // 순이익 = 총 매출액 - 총 원가
        double profitMargin = 0.0;
        if (totalSales > 0) {
            profitMargin = Math.round(((double) netProfit / totalSales * 100) * 10.0) / 10.0; // 마진율 (%)
        }
        
        
        result.put("cumulativeSales", totalSales);
        result.put("averageReceipt", avgReceipt);
        result.put("totalCost", totalCost);          // 총 원가
        result.put("netProfit", netProfit);          // 순이익
        result.put("profitMargin", profitMargin);    // 순이익률 (%)
        result.put("cumulativeSales", totalSales);
        result.put("averageReceipt", avgReceipt);

        // 💡 [추가] 이전 동일 기간(Previous Period) 증감률 계산 로직
        LocalDateTime prevStart = start.minusDays(daysPeriod);
        LocalDateTime prevEnd = start;

        List<Object[]> prevSummaryList = (List<Object[]>) salesRepository.getSummaryStats(branchId, prevStart, prevEnd);
        Long prevTotalSales = 0L;
        Long prevTotalCount = 0L;
        if (prevSummaryList != null && !prevSummaryList.isEmpty()) {
            Object[] prevSummary = prevSummaryList.get(0);
            prevTotalSales = (prevSummary[0] != null) ? ((Number) prevSummary[0]).longValue() : 0L;
            prevTotalCount = (prevSummary[1] != null) ? ((Number) prevSummary[1]).longValue() : 0L;
        }
        Long prevAvgReceipt = prevTotalCount > 0 ? prevTotalSales / prevTotalCount : 0L;

        // 매출액 성장률 (%)
        double growthSalesRate = 0.0;
        if (prevTotalSales > 0) {
            growthSalesRate = Math.round(((double) (totalSales - prevTotalSales) / prevTotalSales * 100) * 10.0) / 10.0;
        }

        // 평균 객단가 성장률 (%)
        double growthReceiptRate = 0.0;
        if (prevAvgReceipt > 0) {
            growthReceiptRate = Math.round(((double) (avgReceipt - prevAvgReceipt) / prevAvgReceipt * 100) * 10.0) / 10.0;
        }

        result.put("growthSalesRate", growthSalesRate);     // 예: 12.4 또는 -3.2
        result.put("growthReceiptRate", growthReceiptRate); // 예: 2.1 또는 -1.5

        // 2. [차트 1] 상단용: 기간별 전체 매출 통계 추이
        List<Map<String, Object>> periodTrends = new ArrayList<>();

        if ("today".equals(filter)) {
            List<Object[]> hourlyRawForPeriod = salesRepository.getHourlySales(branchId, start, end);
            
            Map<Integer, Long> todayMap = new HashMap<>();
            for (int i = 10; i <= 23; i++) todayMap.put(i, 0L);
            for (Object[] row : hourlyRawForPeriod) {
                if (row[0] != null) {
                    todayMap.put(((Number) row[0]).intValue(), ((Number) row[1]).longValue());
                }
            }
            for (int i = 10; i <= 23; i++) {
                Map<String, Object> point = new HashMap<>();
                point.put("axisLabel", i + "시");
                point.put("amount", todayMap.get(i));
                periodTrends.add(point);
            }
        } else {
            // 7일, 30일, 사용자 지정(custom)일 때 일별(Daily) 집계
            List<Object[]> dailyRaw = salesRepository.getDailySales(branchId, start, end);
            for (Object[] row : dailyRaw) {
                Map<String, Object> point = new HashMap<>();
                point.put("axisLabel", row[0].toString());
                point.put("amount", row[1]);
                periodTrends.add(point);
            }
        }
        result.put("periodTrends", periodTrends);

        // 3. [차트 2] 시간대별 분석 (선택 기간 일수로 나눠서 평균 금액 계산)
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
            
            // 오늘 하루 조회가 아닌 다일 조회(7일, 30일, custom)일 경우 일평균 환산
            point.put("amount", "today".equals(filter) ? amount : amount / daysPeriod);
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