package com.sweetscoop.inventory.service;

import com.sweetscoop.inventory.entity.ScmBranchInventory;
import com.sweetscoop.inventory.entity.ScmHqInventory;
import com.sweetscoop.inventory.entity.ScmHqStock;
import com.sweetscoop.inventory.entity.ScmItem;
import com.sweetscoop.inventory.repository.ScmBranchInventoryRepository;
import com.sweetscoop.inventory.repository.ScmBranchRepository;
import com.sweetscoop.inventory.repository.ScmHqInventoryRepository;
import com.sweetscoop.inventory.repository.ScmHqStockRepository;
import com.sweetscoop.inventory.repository.ScmItemRepository;
import com.sweetscoop.menu.dto.MenuDTO;
import com.sweetscoop.menu.repository.MenuDAO;
import com.sweetscoop.size.dto.SizeDTO;
import com.sweetscoop.size.service.SizeService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryService {

	private static final int BRANCH_AUTO_ORDER_THRESHOLD = 5000;
	private static final int BRANCH_AUTO_ORDER_AMOUNT = 3000;

	private static final int HQ_MOCHI_THRESHOLD = 500;
	private static final int HQ_WEIGHT_THRESHOLD = 50000;

	private static final int HQ_MOCHI_AUTO_SUPPLY_AMOUNT = 2000;
	private static final int HQ_WEIGHT_AUTO_SUPPLY_AMOUNT = 200000;

	private final ScmBranchInventoryRepository branchInventoryRepository;
	private final ScmHqInventoryRepository hqInventoryRepository;
	private final ScmHqStockRepository hqStockRepository;
	private final ScmBranchRepository branchRepository;
	private final ScmItemRepository itemRepository;

	private final SizeService sizeService;
	private final MenuDAO menuDAO;

	/**
	 * 지점별 재고 엔티티 조회
	 */
	public List<ScmBranchInventory> getBranchInventory(Integer branchId) {
		validateId(branchId, "지점 ID");

		return branchInventoryRepository.findByBranchId(branchId);
	}

	/**
	 * 물품명, 카테고리명 등이 포함된 지점 재고 조회
	 */
	public List<Map<String, Object>> getBranchInventoryWithNames(Integer branchId) {
		validateId(branchId, "지점 ID");

		List<Object[]> rawData = branchInventoryRepository.findInventoryWithItemName(branchId);

		List<Map<String, Object>> result = new ArrayList<>();

		for (Object[] row : rawData) {
			if (row == null || row.length < 5) {
				continue;
			}

			Map<String, Object> inventory = new HashMap<>();

			inventory.put("itemId", row[0]);
			inventory.put("itemName", row[1]);
			inventory.put("categoryName", row[2]);
			inventory.put("stockLevel", row[3]);
			inventory.put("unit", row[4]);

			result.add(inventory);
		}

		return result;
	}

	/**
	 * 지점 재고 입고
	 */
	@Transactional
	public void importStock(Integer branchId, Integer itemId, int amount) {
		validateId(branchId, "지점 ID");
		validateId(itemId, "물품 ID");
		validatePositiveAmount(amount);

		ScmBranchInventory inventory = getOrCreateBranchInventory(branchId, itemId);

		inventory.increaseStock(amount);

		branchInventoryRepository.save(inventory);
	}

	/**
	 * 지점 재고 단독 출고 및 자동 발주 확인
	 */
	@Transactional
	public void exportStock(Integer branchId, Integer itemId, int amount) {
		validateId(branchId, "지점 ID");
		validateId(itemId, "물품 ID");
		validatePositiveAmount(amount);

		ScmBranchInventory inventory = branchInventoryRepository.findByBranchIdAndItemId(branchId, itemId)
				.orElseThrow(() -> new IllegalArgumentException(
						"지점 재고 정보가 존재하지 않습니다. " + "(지점: " + branchId + ", 물품: " + itemId + ")"));

		inventory.decreaseStock(amount);

		branchInventoryRepository.save(inventory);

		if (inventory.getStockLevel() < BRANCH_AUTO_ORDER_THRESHOLD) {
			checkAndTriggerAutoOrder(branchId, itemId);
		}
	}

	/**
	 * 주문 완료 시 선택 메뉴별 원자재 재고 차감
	 */
	@Transactional
	public void exportStockForOrder(Integer branchId, Integer sizeId, List<Integer> selectedMenuIds) {
		validateId(branchId, "지점 ID");
		validateId(sizeId, "사이즈 ID");

		if (selectedMenuIds == null || selectedMenuIds.isEmpty()) {
			return;
		}

		SizeDTO size = sizeService.getSize(sizeId);

		if (size == null) {
			throw new IllegalArgumentException("존재하지 않는 용기 사이즈입니다.");
		}

		int totalWeightG = size.getTotalWeightG();

		int flavorCount = size.getFlavorCnt();

		if (totalWeightG <= 0) {
			throw new IllegalArgumentException("용기의 총 중량이 올바르지 않습니다.");
		}

		if (flavorCount <= 0) {
			throw new IllegalArgumentException("용기의 선택 가능 맛 수량이 올바르지 않습니다.");
		}

		int baseWeightPerFlavor = totalWeightG / flavorCount;

		if (baseWeightPerFlavor <= 0) {
			throw new IllegalArgumentException("맛별 차감 중량을 계산할 수 없습니다.");
		}

		Map<Integer, Integer> menuSelectCountMap = new HashMap<>();

		for (Integer menuId : selectedMenuIds) {
			validateId(menuId, "메뉴 ID");

			menuSelectCountMap.put(menuId, menuSelectCountMap.getOrDefault(menuId, 0) + 1);
		}

		for (Map.Entry<Integer, Integer> entry : menuSelectCountMap.entrySet()) {
			Integer menuId = entry.getKey();

			Integer selectCount = entry.getValue();

			MenuDTO menu = menuDAO.findById(menuId);

			if (menu == null || menu.getItemId() == null) {
				throw new IllegalArgumentException("메뉴에 연결된 원자재 정보가 없습니다. " + "(Menu ID: " + menuId + ")");
			}

			int finalDeductAmount = Math.multiplyExact(baseWeightPerFlavor, selectCount);

			exportStock(branchId, menu.getItemId(), finalDeductAmount);
		}
	}

	/**
	 * 지점 재고 부족 시 자동 발주 생성
	 */
	private void checkAndTriggerAutoOrder(Integer branchId, Integer itemId) {
		boolean hasPendingOrder = hqInventoryRepository.existsByBranchIdAndItemIdAndApprovalStatus(branchId, itemId,
				"PENDING");

		if (hasPendingOrder) {
			return;
		}

		ScmHqInventory autoOrder = new ScmHqInventory();

		autoOrder.setBranchId(branchId);
		autoOrder.setItemId(itemId);
		autoOrder.setApprovalStatus("PENDING");
		autoOrder.setDeliveryStatus("PREPARING");
		autoOrder.setRequestQuantity(BRANCH_AUTO_ORDER_AMOUNT);

		hqInventoryRepository.save(autoOrder);
	}

	/**
	 * 지점 수동 발주 생성
	 */
	@Transactional
	public void createManualOrder(Integer branchId, Integer itemId, int amount) {
		validateId(branchId, "지점 ID");
		validateId(itemId, "물품 ID");
		validatePositiveAmount(amount);

		branchRepository.findById(branchId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지점입니다. " + "ID: " + branchId));

		itemRepository.findById(itemId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 원자재입니다. " + "ID: " + itemId));

		ScmHqInventory manualOrder = new ScmHqInventory();

		manualOrder.setBranchId(branchId);
		manualOrder.setItemId(itemId);
		manualOrder.setApprovalStatus("PENDING");
		manualOrder.setDeliveryStatus("PREPARING");
		manualOrder.setRequestQuantity(amount);

		hqInventoryRepository.save(manualOrder);
	}

	/**
	 * 지점 발주 승인
	 *
	 * 승인 시: - 본사 재고 차감 - 승인 상태 COMPLETED - 배송 상태 PREPARING
	 *
	 * 지점 재고 증가는 배송완료 처리 시 수행
	 */
	@Transactional
	public void importHqOrder(Integer hqInventoryId) {
		validateId(hqInventoryId, "본사 발주 ID");

		ScmHqInventory hqOrder = hqInventoryRepository.findById(hqInventoryId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 발주 건입니다. " + "ID: " + hqInventoryId));

		String approvalStatus = normalizeStatus(hqOrder.getApprovalStatus());

		if ("COMPLETED".equals(approvalStatus) || "APPROVED".equals(approvalStatus)
				|| "승인완료".equals(hqOrder.getApprovalStatus())) {
			throw new IllegalStateException("이미 승인된 발주 건입니다.");
		}

		if (isDeliveryCompleted(hqOrder.getDeliveryStatus())) {
			throw new IllegalStateException("이미 배송이 완료된 발주 건입니다.");
		}

		int requestQuantity = hqOrder.getRequestQuantity();

		validatePositiveAmount(requestQuantity);

		ScmHqStock hqStock = hqStockRepository.findByItemId(hqOrder.getItemId()).orElseThrow(
				() -> new IllegalArgumentException("본사 창고에 등록되지 않은 물품입니다. " + "물품 ID: " + hqOrder.getItemId()));

		ScmItem item = itemRepository.findById(hqOrder.getItemId()).orElseThrow(
				() -> new IllegalArgumentException("원자재 정보가 존재하지 않습니다. " + "물품 ID: " + hqOrder.getItemId()));

		hqStock.decreaseStock(requestQuantity);

		hqStockRepository.save(hqStock);

		hqOrder.setApprovalStatus("COMPLETED");
		hqOrder.setDeliveryStatus("PREPARING");

		hqInventoryRepository.save(hqOrder);

		int threshold = isMochiCategory(item) ? HQ_MOCHI_THRESHOLD : HQ_WEIGHT_THRESHOLD;

		if (hqStock.getStockLevel() < threshold) {
			triggerHqAutoOrderFromFactory(item);
		}
	}

	/**
	 * 본사 창고 재고 충전
	 */
	@Transactional
	public void chargeHqStock(Integer itemId, int amount) {
		validateId(itemId, "물품 ID");
		validatePositiveAmount(amount);

		itemRepository.findById(itemId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 원자재입니다. " + "ID: " + itemId));

		ScmHqStock hqStock = hqStockRepository.findByItemId(itemId).orElseGet(() -> {
			ScmHqStock newStock = new ScmHqStock();

			newStock.setItemId(itemId);
			newStock.setStockLevel(0);

			return hqStockRepository.save(newStock);
		});

		hqStock.increaseStock(amount);

		hqStockRepository.save(hqStock);
	}

	/**
	 * 전체 본사 발주 목록을 지점명과 원자재명 포함 형태로 조회
	 */
	public List<Map<String, Object>> getAllHqOrdersWithNames() {

		List<ScmHqInventory> rawOrders = hqInventoryRepository.findAll();

		Map<Integer, String> branchNameMap = new HashMap<>();

		branchRepository.findAll().forEach(branch -> branchNameMap.put(branch.getId(), branch.getBranchName()));

		Map<Integer, String> itemNameMap = new HashMap<>();

		itemRepository.findAll().forEach(item -> itemNameMap.put(item.getId(), item.getItemName()));

		List<Map<String, Object>> result = new ArrayList<>();

		for (ScmHqInventory order : rawOrders) {
			Map<String, Object> orderMap = new HashMap<>();

			Integer branchId = order.getBranchId();

			Integer itemId = order.getItemId();

			orderMap.put("hqInventoryId", order.getId());

			/*
			 * 프론트엔드에서 requestId를 사용하는 경우를 위해 같은 값을 함께 제공
			 */
			orderMap.put("requestId", order.getId());

			orderMap.put("branchId", branchId);

			orderMap.put("branchName", branchNameMap.getOrDefault(branchId, "미등록 지점(#" + branchId + ")"));

			orderMap.put("itemId", itemId);

			orderMap.put("itemName", itemNameMap.getOrDefault(itemId, "미등록 원자재(#" + itemId + ")"));

			orderMap.put("requestQuantity", order.getRequestQuantity());

			orderMap.put("approvalStatus", order.getApprovalStatus());

			orderMap.put("deliveryStatus", order.getDeliveryStatus());

			result.add(orderMap);
		}

		return result;
	}

	/**
	 * 배송 상태 변경
	 *
	 * 배송완료 상태로 최초 변경될 때에만 지점 재고를 증가시킨다.
	 */
	@Transactional
	public void updateDeliveryStatus(Integer requestId, String deliveryStatus) {
		validateId(requestId, "배송 요청 ID");

		String normalizedNewStatus = normalizeDeliveryStatus(deliveryStatus);

		ScmHqInventory hqOrder = hqInventoryRepository.findById(requestId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 배송 요청입니다. " + "ID: " + requestId));

		String previousStatus = normalizeDeliveryStatus(hqOrder.getDeliveryStatus());

		if ("PENDING".equals(normalizeStatus(hqOrder.getApprovalStatus()))) {
			throw new IllegalStateException("승인되지 않은 발주 건은 배송 상태를 변경할 수 없습니다.");
		}

		/*
		 * 이미 배송완료인 건은 다시 완료 처리하지 않도록 방지
		 */
		if ("ARRIVED".equals(previousStatus) && "ARRIVED".equals(normalizedNewStatus)) {
			return;
		}

		validateDeliveryTransition(previousStatus, normalizedNewStatus);

		boolean firstCompletion = !"ARRIVED".equals(previousStatus) && "ARRIVED".equals(normalizedNewStatus);

		hqOrder.setDeliveryStatus(normalizedNewStatus);

		hqInventoryRepository.save(hqOrder);

		/*
		 * 배송이 처음 완료되는 순간에만 지점 재고 증가
		 */
		if (firstCompletion) {
			increaseBranchStockFromDelivery(hqOrder);
		}
	}

	/**
	 * 배송 완료에 따른 지점 재고 증가
	 */
	private void increaseBranchStockFromDelivery(ScmHqInventory hqOrder) {
		int requestQuantity = hqOrder.getRequestQuantity();

		validatePositiveAmount(requestQuantity);

		ScmBranchInventory branchInventory = getOrCreateBranchInventory(hqOrder.getBranchId(), hqOrder.getItemId());

		branchInventory.increaseStock(requestQuantity);

		branchInventoryRepository.save(branchInventory);

		System.out.println("[배송 완료] 지점(" + hqOrder.getBranchId() + ") / 원자재(" + hqOrder.getItemId() + ") / 입고 수량: "
				+ requestQuantity);
	}

	/**
	 * 지점 재고가 없으면 새로 생성
	 */
	private ScmBranchInventory getOrCreateBranchInventory(Integer branchId, Integer itemId) {

		return branchInventoryRepository.findByBranchIdAndItemId(branchId, itemId).orElseGet(() -> {
			ScmBranchInventory newInventory = new ScmBranchInventory();

			newInventory.setBranchId(branchId);
			newInventory.setItemId(itemId);
			newInventory.setStockLevel(0);

			return branchInventoryRepository.save(newInventory);
		});
	}

	/**
	 * 배송 상태 전이 검증
	 */
	private void validateDeliveryTransition(String previousStatus, String newStatus) {
		if (previousStatus.equals(newStatus)) {
			return;
		}

		boolean validTransition = ("PREPARING".equals(previousStatus) && "SHIPPING".equals(newStatus))
				|| ("SHIPPING".equals(previousStatus) && "ARRIVED".equals(newStatus));

		if (!validTransition) {
			throw new IllegalStateException("허용되지 않는 배송 상태 변경입니다. " + previousStatus + " → " + newStatus);
		}
	}

	/**
	 * 프론트엔드와 DB의 여러 배송 상태 표현을 표준화
	 */
	private String normalizeDeliveryStatus(String status) {
		if (status == null || status.isBlank()) {
			return "PREPARING";
		}

		String normalized = status.trim().toUpperCase();

		return switch (normalized) {
		case "준비중", "준비 중", "배송준비중", "배송 준비중", "READY", "WAITING", "PREPARING" -> "PREPARING";

		case "배송중", "배송 중", "IN_TRANSIT", "SHIPPED", "SHIPPING" -> "SHIPPING";

		case "배송완료", "배송 완료", "DELIVERED", "ARRIVED", "COMPLETED" -> "ARRIVED";

		default -> throw new IllegalArgumentException("지원하지 않는 배송 상태입니다: " + status);
		};
	}

	/**
	 * 배송 완료 여부
	 */
	private boolean isDeliveryCompleted(String status) {
		try {
			return "ARRIVED".equals(normalizeDeliveryStatus(status));
		} catch (IllegalArgumentException exception) {
			return false;
		}
	}

	/**
	 * 일반 상태 문자열 정규화
	 */
	private String normalizeStatus(String status) {
		if (status == null) {
			return "";
		}

		return status.trim().toUpperCase();
	}

	/**
	 * 모찌 카테고리 여부
	 */
	private boolean isMochiCategory(ScmItem item) {
		return item.getCategoryId() != null && item.getCategoryId() == 2;
	}

	/**
	 * 본사 재고 부족 시 공장 자동 보충 요청
	 */
	private void triggerHqAutoOrderFromFactory(ScmItem item) {
		boolean mochiCategory = isMochiCategory(item);

		int autoSupplyAmount = mochiCategory ? HQ_MOCHI_AUTO_SUPPLY_AMOUNT : HQ_WEIGHT_AUTO_SUPPLY_AMOUNT;

		String unitName = mochiCategory ? "개" : "g";

		System.out.println("본사 창고 물품 [" + item.getItemName() + " (ID: #" + item.getId() + ")] 재고 부족 감지");

		System.out.println("공장 제조 라인에 " + autoSupplyAmount + unitName + " 자동 보충 요청 전송 완료");
	}

	private void validateId(Integer id, String fieldName) {
		if (id == null || id <= 0) {
			throw new IllegalArgumentException(fieldName + "는 1 이상의 값이어야 합니다.");
		}
	}

	private void validatePositiveAmount(int amount) {
		if (amount <= 0) {
			throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
		}
	}
}