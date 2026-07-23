package com.sweetscoop.payment.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.sweetscoop.coupon.entity.Coupon;
import com.sweetscoop.coupon.repository.CouponRepository;
import com.sweetscoop.firebase.FirebaseService;
import com.sweetscoop.inventory.service.InventoryService;
import com.sweetscoop.member.entity.Member;
import com.sweetscoop.member.repository.MemberRepository;
import com.sweetscoop.payment.dto.PaymentCalculationRequestDTO;
import com.sweetscoop.payment.dto.PaymentCalculationResponseDTO;
import com.sweetscoop.payment.dto.PaymentRequestDTO;
import com.sweetscoop.payment.repository.PaymentMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {

	/**
	 * 결제 금액의 5%를 포인트로 적립
	 */
	private static final double POINT_EARNING_RATE = 0.05;

	private final PaymentMapper paymentMapper;

	private final PaymentCalculationService paymentCalculationService;

	private final MemberRepository memberRepository;

	private final CouponRepository couponRepository;

	private final RestTemplate restTemplate;

	private final FirebaseService firebaseService;
	
	private final InventoryService inventoryService;

	/**
	 * application.properties 또는 환경변수에 설정된 Toss Secret Key
	 */
	@Value("${toss.secret-key}")
	private String tossSecretKey;

	/**
	 * Toss 결제 승인 및 결제 완료 처리
	 */
	@Transactional(rollbackFor = Exception.class)
	public Map<String, Object> processTossPayment(PaymentRequestDTO dto) throws Exception {

		/*
		 * 1. 결제 요청값 검증
		 */
		validateRequest(dto);

		/*
		 * 2. 주문 행 잠금
		 *
		 * 동일 주문에 결제 요청이 동시에 들어오는 것을 방지한다.
		 */
		Map<String, Object> order = paymentMapper.selectOrderForUpdate(dto.getOrderId());

		if (order == null || order.isEmpty()) {
			throw new IllegalArgumentException("존재하지 않는 주문 번호입니다.");
		}

		/*
		 * 3. 이미 결제된 주문인지 검사
		 */
		validateOrderStatus(order);

		/*
		 * 4. ORDERS에 저장된 할인 전 금액 조회
		 */
		int originalAmount = getRequiredInteger(order, "totalPrice", "total_price");

		/*
		 * 5. 회원 조회
		 *
		 * 전화번호가 없으면 비회원으로 처리한다.
		 */
		Member member = findMember(dto.getPhoneNumber());

		/*
		 * 6. 서버 기준 쿠폰·포인트 할인 금액 계산
		 */
		PaymentCalculationResponseDTO calculation = calculatePayment(dto, originalAmount);

		/*
		 * 7. 프론트 결제 금액과 서버 계산 금액 비교
		 */
		validatePaymentAmount(dto.getAmount(), calculation.getFinalAmount());

		/*
		 * Mapper에 저장할 포인트 사용 금액 설정
		 */
		dto.setPointUsed(calculation.getPointDiscount());

		/*
		 * 결제 금액에 따른 적립 포인트 계산
		 */
		int pointEarned = calculatePointEarned(calculation.getFinalAmount(), member);

		dto.setPointEarned(pointEarned);

		/*
		 * 8. 선택한 쿠폰 최종 검증
		 */
		Coupon coupon = validateSelectedCoupon(dto.getCouponId(), member);

		/*
		 * 9. Toss Payments 결제 승인
		 */
		Map<String, Object> tossResponse = confirmTossPayment(dto);

		/*
		 * 10. Toss 응답에서 결제수단과 카드사 추출
		 */
		setPaymentMethod(dto, tossResponse);

		setCardCompany(dto, tossResponse);

		/*
		 * PAYMENT NOT NULL 컬럼의 기본값 설정
		 */
		applyPaymentDefaults(dto);

		/*
		 * 11. PAYMENT 테이블 저장
		 */
		int paymentResult = paymentMapper.insertPayment(dto, dto.getPaymentKey());

		if (paymentResult <= 0) {
			throw new IllegalStateException("PAYMENT 내역 생성에 실패했습니다.");
		}

		/*
		 * 12. 결제 승인 후 쿠폰 사용 처리
		 */
		if (coupon != null) {
			useCoupon(coupon);
		}

		/*
		 * 13. 회원 포인트 차감·적립 및 주문 횟수 증가
		 */
		if (member != null) {
			updateMemberPoint(member, calculation.getPointDiscount(), pointEarned);
		}

		/*
		 * 14. 순차 대기번호 생성
		 */
		int waitingNo = createWaitingNumber();

		/*
		 * 15. 주문 상태 및 대기번호 갱신
		 */
		int orderResult = paymentMapper.updateOrderStatus(dto.getOrderId(), "결제완료", waitingNo);
		
		if (orderResult <= 0) {
			throw new IllegalStateException("ORDERS 상태 업데이트에 실패했습니다.");
		}
		
		try {
			List<Map<String, Object>> items = paymentMapper.selectOrderItems(dto.getOrderId());
			int branchId = convertToInteger(getMapValue(order, "branchId", "branch_id"), 1);

			for (Map<String, Object> item : items) {
				// 1. sizeId 추출
				Object sizeIdObj = getMapValue(item, "sizeId", "size_id");
				Integer sizeId = convertToInteger(sizeIdObj, 0);

				// 2. Mapper에서 새롭게 조회한 menuIds("1,2,3") 추출
				Object menuIdsObj = getMapValue(item, "menuIds", "menu_ids");
				List<Integer> selectedMenuIds = new java.util.ArrayList<>();

				if (menuIdsObj != null) {
					String menuStr = String.valueOf(menuIdsObj);
					for (String idStr : menuStr.split(",")) {
						try {
							selectedMenuIds.add(Integer.parseInt(idStr.trim()));
						} catch (NumberFormatException ignored) {}
					}
				}

				// 3. 재고 차감 실행
				if (sizeId > 0 && !selectedMenuIds.isEmpty()) {
					inventoryService.exportStockForOrder(branchId, sizeId, selectedMenuIds);
				}
			}
		} catch (Exception e) {
			System.err.println("❌ [재고 차감 중 에러 발생]: " + e.getMessage());
			e.printStackTrace();
		}

		/*
		 * 16. 영수증 데이터 조회
		 */
		Map<String, Object> receiptData = createReceiptData(dto.getOrderId());

		/*
		 * 17. Firebase로 결제 완료 주문 전송
		 *
		 * Firebase 전송 실패는 실제 결제 결과를 롤백하지 않도록 별도 예외 처리한다.
		 */
		sendReceiptToFirebase(dto, order, receiptData);

		/*
		 * 18. 프론트에 영수증 데이터 반환
		 */
		return receiptData;
	}

	/**
	 * 결제 요청 기본값 검증
	 */
	private void validateRequest(PaymentRequestDTO dto) {

		if (dto == null) {
			throw new IllegalArgumentException("결제 요청 데이터가 없습니다.");
		}

		if (dto.getOrderId() == null || dto.getOrderId().isBlank()) {
			throw new IllegalArgumentException("DB 주문번호가 없습니다.");
		}

		if (dto.getTossOrderId() == null || dto.getTossOrderId().isBlank()) {
			throw new IllegalArgumentException("Toss 주문번호가 없습니다.");
		}

		if (dto.getPaymentKey() == null || dto.getPaymentKey().isBlank()) {
			throw new IllegalArgumentException("Toss 결제키가 없습니다.");
		}

		if (dto.getAmount() <= 0) {
			throw new IllegalArgumentException("결제 금액은 1원 이상이어야 합니다.");
		}

		if (dto.getPointUsed() != null && dto.getPointUsed() < 0) {
			throw new IllegalArgumentException("사용 포인트는 0 이상이어야 합니다.");
		}

		if (dto.getPointUsed() != null && dto.getPointUsed() % 500 != 0) {
			throw new IllegalArgumentException("포인트는 500원 단위로만 사용할 수 있습니다.");
		}
	}

	/**
	 * 주문 상태 검증
	 */
	private void validateOrderStatus(Map<String, Object> order) {

		Object statusValue = getMapValue(order, "status", "STATUS");

		if (statusValue == null) {
			return;
		}

		String status = String.valueOf(statusValue);

		if ("결제완료".equals(status) || "PAYMENT_COMPLETE".equals(status) || "DONE".equals(status)) {
			throw new IllegalStateException("이미 결제가 완료된 주문입니다.");
		}
	}

	/**
	 * 전화번호로 회원 조회
	 */
	private Member findMember(String phoneNumber) {

		if (phoneNumber == null || phoneNumber.isBlank()) {
			return null;
		}

		String normalizedPhoneNumber = phoneNumber.replaceAll("[^0-9]", "");

		return memberRepository.findByPhoneNumber(normalizedPhoneNumber)
				.orElseThrow(() -> new IllegalArgumentException("전화번호에 해당하는 회원을 찾을 수 없습니다."));
	}

	/**
	 * 서버 기준 할인 금액 계산
	 */
	private PaymentCalculationResponseDTO calculatePayment(PaymentRequestDTO dto, int originalAmount) {

		PaymentCalculationRequestDTO request = new PaymentCalculationRequestDTO();

		request.setPhoneNumber(dto.getPhoneNumber());

		request.setOriginalAmount(originalAmount);

		request.setPointUsed(dto.getPointUsed() == null ? 0 : dto.getPointUsed());

		request.setCouponId(dto.getCouponId());

		return paymentCalculationService.calculate(request);
	}

	/**
	 * 프론트 요청 금액과 서버 계산 금액 비교
	 */
	private void validatePaymentAmount(int requestedAmount, Integer calculatedAmount) {

		if (calculatedAmount == null) {
			throw new IllegalStateException("최종 결제 금액을 계산하지 못했습니다.");
		}

		if (calculatedAmount <= 0) {
			throw new IllegalArgumentException("최종 결제 금액은 1원 이상이어야 합니다.");
		}

		if (requestedAmount != calculatedAmount) {
			throw new IllegalArgumentException(
					"결제 금액이 일치하지 않습니다. " + "요청 금액=" + requestedAmount + ", 서버 계산 금액=" + calculatedAmount);
		}
	}

	/**
	 * 선택한 쿠폰 최종 검증
	 */
	private Coupon validateSelectedCoupon(Integer couponId, Member member) {

		if (couponId == null) {
			return null;
		}

		if (member == null) {
			throw new IllegalArgumentException("비회원은 쿠폰을 사용할 수 없습니다.");
		}

		return paymentCalculationService.getValidatedCoupon(couponId, member.getId());
	}

	/**
	 * Toss Payments 결제 승인 요청
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Object> confirmTossPayment(PaymentRequestDTO dto) throws Exception {

		String secretKeyWithColon = tossSecretKey.endsWith(":") ? tossSecretKey : tossSecretKey + ":";

		String basicAuthHeader = Base64.getEncoder()
				.encodeToString(secretKeyWithColon.getBytes(StandardCharsets.UTF_8));

		HttpHeaders headers = new HttpHeaders();

		headers.setContentType(MediaType.APPLICATION_JSON);

		headers.set(HttpHeaders.AUTHORIZATION, "Basic " + basicAuthHeader);

		Map<String, Object> tossBody = new HashMap<>();

		tossBody.put("paymentKey", dto.getPaymentKey());

		tossBody.put("amount", dto.getAmount());

		tossBody.put("orderId", dto.getTossOrderId());

		HttpEntity<Map<String, Object>> entity = new HttpEntity<>(tossBody, headers);

		try {
			ResponseEntity<Map> response = restTemplate
					.postForEntity("https://api.tosspayments.com/v1/payments/confirm", entity, Map.class);

			if (response.getStatusCode() != HttpStatus.OK) {
				throw new IllegalStateException("Toss 승인 응답 상태가 정상적이지 않습니다: " + response.getStatusCode());
			}

			if (response.getBody() == null) {
				throw new IllegalStateException("Toss 승인 응답 본문이 없습니다.");
			}

			return (Map<String, Object>) response.getBody();

		} catch (Exception exception) {
			throw new Exception("Toss Payments 승인 연동 실패: " + exception.getMessage(), exception);
		}
	}

	/**
	 * 결제 수단 추출
	 */
	private void setPaymentMethod(PaymentRequestDTO dto, Map<String, Object> tossResponse) {

		Object methodValue = tossResponse.get("method");

		if (methodValue == null) {
			return;
		}

		String method = String.valueOf(methodValue);

		if (!method.isBlank()) {
			dto.setMethod(method);
		}
	}

	/**
	 * 카드사 또는 간편결제 제공사 추출
	 */
	private void setCardCompany(PaymentRequestDTO dto, Map<String, Object> tossResponse) {

		Object cardValue = tossResponse.get("card");

		if (cardValue instanceof Map<?, ?> cardMap) {
			Object companyValue = cardMap.get("company");

			if (companyValue != null) {
				String company = String.valueOf(companyValue);

				if (!company.isBlank()) {
					dto.setCardCompany(company);

					return;
				}
			}
		}

		Object easyPayValue = tossResponse.get("easyPay");

		if (easyPayValue instanceof Map<?, ?> easyPayMap) {
			Object providerValue = easyPayMap.get("provider");

			if (providerValue != null) {
				String provider = String.valueOf(providerValue);

				if (!provider.isBlank()) {
					dto.setCardCompany(provider);
				}
			}
		}
	}

	/**
	 * PAYMENT NOT NULL 컬럼 기본값
	 */
	private void applyPaymentDefaults(PaymentRequestDTO dto) {

		if (dto.getMethod() == null || dto.getMethod().isBlank()) {
			dto.setMethod("카드");
		}

		if (dto.getCardCompany() == null || dto.getCardCompany().isBlank()) {
			dto.setCardCompany("토스페이먼츠");
		}

		if (dto.getPointUsed() == null) {
			dto.setPointUsed(0);
		}

		if (dto.getPointEarned() == null) {
			dto.setPointEarned(0);
		}
	}

	/**
	 * 쿠폰 사용 처리
	 */
	private void useCoupon(Coupon coupon) {

		if (Boolean.TRUE.equals(coupon.getIsUsed())) {
			throw new IllegalStateException("이미 사용한 쿠폰입니다.");
		}

		if (coupon.getExpiryDate() != null && coupon.getExpiryDate().isBefore(LocalDateTime.now())) {
			throw new IllegalStateException("사용 기간이 만료된 쿠폰입니다.");
		}

		coupon.setIsUsed(true);

		coupon.setUsedAt(LocalDateTime.now());

		couponRepository.save(coupon);
	}

	/**
	 * 회원 포인트 차감·적립 및 주문 횟수 증가
	 */
	private void updateMemberPoint(Member member, Integer pointUsed, Integer pointEarned) {

		int currentPoint = member.getPoint() == null ? 0 : member.getPoint();

		int usedPoint = pointUsed == null ? 0 : pointUsed;

		int earnedPoint = pointEarned == null ? 0 : pointEarned;

		if (usedPoint < 0) {
			throw new IllegalArgumentException("사용 포인트가 올바르지 않습니다.");
		}

		if (usedPoint % 500 != 0) {
			throw new IllegalArgumentException("포인트는 500원 단위로만 사용할 수 있습니다.");
		}

		if (usedPoint > currentPoint) {
			throw new IllegalArgumentException("보유 포인트가 부족합니다.");
		}

		member.setPoint(currentPoint - usedPoint + earnedPoint);

		int currentOrderCount = member.getOrderCount() == null ? 0 : member.getOrderCount();

		member.setOrderCount(currentOrderCount + 1);

		memberRepository.save(member);
	}

	/**
	 * 적립 포인트 계산
	 *
	 * 회원만 실제 결제 금액의 5%를 적립한다.
	 */
	private int calculatePointEarned(int finalAmount, Member member) {

		if (member == null) {
			return 0;
		}

		return (int) Math.floor(finalAmount * POINT_EARNING_RATE);
	}

	/**
	 * 순차 대기번호 생성
	 *
	 * 현재 ORDERS의 최대 대기번호에 1을 더한다.
	 */
	private int createWaitingNumber() {

		Integer maxWaitingNo = paymentMapper.selectMaxWaitingNo();

		if (maxWaitingNo == null || maxWaitingNo < 1) {
			return 1;
		}

		return maxWaitingNo + 1;
	}

	/**
	 * 영수증 데이터 생성
	 */
	private Map<String, Object> createReceiptData(String orderId) {

		Map<String, Object> receiptData = paymentMapper.selectReceiptDetails(orderId);

		if (receiptData == null) {
			receiptData = new HashMap<>();
		}

		List<Map<String, Object>> items = paymentMapper.selectOrderItems(orderId);

		receiptData.put("items", items == null ? List.of() : items);

		return receiptData;
	}

	/**
	 * Firebase에 결제 완료 주문 전달
	 *
	 * Firebase 전송 실패는 결제 처리를 실패시키지 않는다.
	 */
	private void sendReceiptToFirebase(PaymentRequestDTO dto, Map<String, Object> order,
			Map<String, Object> receiptData) {

		try {
			Map<String, Object> firebasePayload = new HashMap<>();

			firebasePayload.put("orderId", dto.getOrderId());

			firebasePayload.put("orderNo", getMapValue(receiptData, "receiptNo", "receipt_no"));

			firebasePayload.put("waitingNo", getMapValue(receiptData, "waitingNo", "waiting_no"));

			firebasePayload.put("items", receiptData.getOrDefault("items", List.of()));

			firebasePayload.put("status", "결제완료");

			firebasePayload.put("totalPrice", getMapValue(receiptData, "finalAmount", "totalPrice", "total_price"));

			firebasePayload.put("paymentMethod", getMapValue(receiptData, "paymentMethod", "payment_method"));

			Object branchValue = getMapValue(order, "branchId", "branch_id");

			int branchId = convertToInteger(branchValue, 1);

			firebaseService.sendOrderToBranch(branchId, dto.getOrderId(), firebasePayload);

			System.out.println(">>> Firebase 주문 전송 완료" + " - branchId: " + branchId + ", orderId: " + dto.getOrderId());

		} catch (Exception exception) {
			System.err.println(">>> Firebase 실시간 주문 전송 실패" + " (결제는 정상 처리됨): " + exception.getMessage());
		}
	}

	/**
	 * Map에서 필수 정수값 추출
	 */
	private int getRequiredInteger(Map<String, Object> map, String... keys) {

		Object value = getMapValue(map, keys);

		if (value instanceof Number number) {
			return number.intValue();
		}

		if (value != null) {
			try {
				return Integer.parseInt(String.valueOf(value));

			} catch (NumberFormatException ignored) {
				// 아래 공통 예외로 처리
			}
		}

		throw new IllegalStateException("주문의 원래 결제 금액을 확인할 수 없습니다.");
	}

	/**
	 * Object 값을 Integer로 변환
	 */
	private int convertToInteger(Object value, int defaultValue) {

		if (value instanceof Number number) {
			return number.intValue();
		}

		if (value != null) {
			try {
				return Integer.parseInt(String.valueOf(value));

			} catch (NumberFormatException ignored) {
				return defaultValue;
			}
		}

		return defaultValue;
	}

	/**
	 * Map의 키 이름 차이를 고려한 값 조회
	 */
	private Object getMapValue(Map<String, Object> map, String... keys) {

		if (map == null) {
			return null;
		}

		for (String key : keys) {
			if (map.containsKey(key)) {
				return map.get(key);
			}

			String upperKey = key.toUpperCase();

			if (map.containsKey(upperKey)) {
				return map.get(upperKey);
			}
		}

		return null;
	}
}