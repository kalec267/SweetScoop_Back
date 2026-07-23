package com.sweetscoop.inventory.service;

import com.sweetscoop.inventory.entity.ScmBranchInventory;
import com.sweetscoop.inventory.entity.ScmHqInventory;
import com.sweetscoop.inventory.entity.ScmHqStock;
import com.sweetscoop.inventory.entity.ScmItem;
import com.sweetscoop.inventory.repository.ScmBranchInventoryRepository;
import com.sweetscoop.inventory.repository.ScmHqInventoryRepository;
import com.sweetscoop.inventory.repository.ScmHqStockRepository;
import com.sweetscoop.inventory.repository.ScmItemRepository;
import com.sweetscoop.inventory.repository.ScmBranchRepository;

import com.sweetscoop.size.dto.SizeDTO;
import com.sweetscoop.size.service.SizeService;
import com.sweetscoop.menu.dto.MenuDTO;
import com.sweetscoop.menu.repository.MenuDAO;

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

	private final ScmBranchInventoryRepository branchInventoryRepository;
	private final ScmHqInventoryRepository hqInventoryRepository;
	private final ScmHqStockRepository hqStockRepository;
	private final ScmBranchRepository branchRepository;
	private final ScmItemRepository itemRepository;

	private final SizeService sizeService;
	private final MenuDAO menuDAO;

	// 지점별 재고 조회
	public List<ScmBranchInventory> getBranchInventory(Integer branchId) {
		return branchInventoryRepository.findByBranchId(branchId);
	}

	// 재고 입고
	@Transactional
	public void importStock(Integer branchId, Integer itemId, int amount) {
		ScmBranchInventory inventory = branchInventoryRepository.findByBranchIdAndItemId(branchId, itemId)
				.orElseGet(() -> {
					ScmBranchInventory newInv = new ScmBranchInventory();
					newInv.setBranchId(branchId);
					newInv.setItemId(itemId);
					return branchInventoryRepository.save(newInv);
				});
		inventory.increaseStock(amount);
	}

	// 단독 출고 및 자동 발주 검사 메서드
	@Transactional
	public void exportStock(Integer branchId, Integer itemId, int amount) {
		ScmBranchInventory inventory = branchInventoryRepository.findByBranchIdAndItemId(branchId, itemId).orElseThrow(
				() -> new IllegalArgumentException("지점 재고 정보가 존재하지 않습니다. (지점: " + branchId + ", 물품: " + itemId + ")"));

		inventory.decreaseStock(amount);

		// 지점 재고가 5,000g 미만일 경우 자동으로 본사 발주 신청
		if (inventory.getStockLevel() < 5000) {
			checkAndTriggerAutoOrder(branchId, itemId);
		}
	}

	@Transactional
	public void exportStockForOrder(Integer branchId, Integer sizeId, List<Integer> selectedMenuIds) {
		if (selectedMenuIds == null || selectedMenuIds.isEmpty()) {
			return;
		}

		// 1. SizeService를 통해 용기 총 중량(totalWeightG)과 선택 가능 맛 수(flavorCnt) 조회
		SizeDTO size = sizeService.getSize(sizeId);
		if (size == null) {
			throw new IllegalArgumentException("존재하지 않는 용기 사이즈입니다.");
		}

		int totalWeightG = size.getTotalWeightG(); // 예: 파인트 336g
		int flavorCnt = size.getFlavorCnt(); // 예: 파인트 3개

		if (flavorCnt <= 0) {
			throw new IllegalArgumentException("용기의 선택 가능 맛 수량이 올바르지 않습니다.");
		}

		// 2. 1개 맛 선택 시 차감할 기본 단위 중량 계산 (예: 336g / 3 = 112g)
		int baseWeightPerFlavor = totalWeightG / flavorCnt;

		// 3. 중복 선택된 메뉴(menuId) 수량 카운팅 (Map<MenuId, 선택수량>)
		Map<Integer, Integer> menuSelectCountMap = new HashMap<>();
		for (Integer menuId : selectedMenuIds) {
			menuSelectCountMap.put(menuId, menuSelectCountMap.getOrDefault(menuId, 0) + 1);
		}

		// 4. 각 메뉴별로 MenuDAO를 이용해 원자재(itemId)를 찾고 (기본 중량 * 선택 수량)만큼 차감
		for (Map.Entry<Integer, Integer> entry : menuSelectCountMap.entrySet()) {
			Integer menuId = entry.getKey();
			Integer selectCount = entry.getValue();

			// MenuDAO를 사용하여 menuId에 연결된 실제 원자재 ID(itemId) 조회
			MenuDTO menu = menuDAO.findById(menuId);
			if (menu == null || menu.getItemId() == null) {
				throw new IllegalArgumentException("메뉴 정보 또는 매핑된 원자재(Item) ID가 존재하지 않습니다. (Menu ID: " + menuId + ")");
			}

			Integer itemId = menu.getItemId();

			// 최종 차감할 중량 계산 (예: 엄마는외계인 112g * 2 = 224g)
			int finalDeductAmount = baseWeightPerFlavor * selectCount;

			// 지점 재고 차감 및 자동 발주 검사 실행
			exportStock(branchId, itemId, finalDeductAmount);
		}
	}

	// 내부 자동 발주 트리거 메서드
	private void checkAndTriggerAutoOrder(Integer branchId, Integer itemId) {
		boolean hasPendingOrder = hqInventoryRepository.existsByBranchIdAndItemIdAndApprovalStatus(branchId, itemId,
				"PENDING");

		if (!hasPendingOrder) {
			ScmHqInventory autoOrder = new ScmHqInventory();
			autoOrder.setBranchId(branchId);
			autoOrder.setItemId(itemId);
			autoOrder.setApprovalStatus("PENDING");
			autoOrder.setDeliveryStatus("PREPARING");
			autoOrder.setRequestQuantity(3000);

			hqInventoryRepository.save(autoOrder);
		}
	}

	// 지점 발주 승인 로직
	@Transactional
	public void importHqOrder(Integer hqInventoryId) {
		ScmHqInventory hqOrder = hqInventoryRepository.findById(hqInventoryId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 발주 건입니다."));

		if ("ARRIVED".equals(hqOrder.getDeliveryStatus()) || "COMPLETED".equals(hqOrder.getApprovalStatus())) {
			throw new IllegalStateException("이미 처리가 완료된 발주 건입니다.");
		}

		ScmHqStock hqStock = hqStockRepository.findByItemId(hqOrder.getItemId())
				.orElseThrow(() -> new IllegalArgumentException("본사 창고에 등록되지 않은 물품입니다."));

		// 본사 재고 차감
		hqStock.decreaseStock(hqOrder.getRequestQuantity());
		hqStockRepository.save(hqStock);

		// ScmItem을 조회해서 categoryId 기반으로 임계치 설정
		ScmItem item = itemRepository.findById(hqOrder.getItemId())
				.orElseThrow(() -> new IllegalArgumentException("원자재(Item) 정보가 존재하지 않습니다."));

		// categoryId가 2(모찌류)이면 500개, 그 외(아이스크림/원두)는 50,000g 기준
		boolean isMochiCategory = (item.getCategoryId() != null && item.getCategoryId() == 2);
		int threshold = isMochiCategory ? 500 : 50000;

		if (hqStock.getStockLevel() < threshold) {
			triggerHqAutoOrderFromFactory(item);
		}

		// 발주 상태 완료 처리
		hqOrder.setDeliveryStatus("ARRIVED");
		hqOrder.setApprovalStatus("COMPLETED");
		hqInventoryRepository.save(hqOrder);

		// 지점 재고 가산
		ScmBranchInventory branchStock = branchInventoryRepository
				.findByBranchIdAndItemId(hqOrder.getBranchId(), hqOrder.getItemId()).orElseGet(() -> {
					ScmBranchInventory newStock = new ScmBranchInventory();
					newStock.setBranchId(hqOrder.getBranchId());
					newStock.setItemId(hqOrder.getItemId());
					newStock.setStockLevel(0);
					return branchInventoryRepository.save(newStock);
				});

		branchStock.increaseStock(hqOrder.getRequestQuantity());
		branchInventoryRepository.save(branchStock);
	}

	private void triggerHqAutoOrderFromFactory(ScmItem item) {
		// categoryId가 2(모찌류)이면 2,000개, 그 외는 200,000g 수급 요청
		boolean isMochiCategory = (item.getCategoryId() != null && item.getCategoryId() == 2);
		int autoSupplyAmount = isMochiCategory ? 2000 : 200000;
		String unitName = isMochiCategory ? "ea" : "g";

		System.out.println("본사 창고 물품 [" + item.getItemName() + " (ID: #" + item.getId() + ")] 재고 부족 감지!");
		System.out.println("공장 제조 라인에 원재료 " + autoSupplyAmount + unitName + " 자동 보충 요청 전송 완료.");
	}

	@Transactional
	public void chargeHqStock(Integer itemId, int amount) {
		ScmHqStock hqStock = hqStockRepository.findByItemId(itemId).orElseGet(() -> {
			ScmHqStock newStock = new ScmHqStock();
			newStock.setItemId(itemId);
			newStock.setStockLevel(0);
			return hqStockRepository.save(newStock);
		});
		hqStock.increaseStock(amount);
		hqStockRepository.save(hqStock);
	}

	public List<Map<String, Object>> getBranchInventoryWithNames(Integer branchId) {
		List<Object[]> rawData = branchInventoryRepository.findInventoryWithItemName(branchId);
		List<Map<String, Object>> result = new ArrayList<>();

		for (Object[] row : rawData) {
			Map<String, Object> map = new HashMap<>();
			map.put("itemId", row[0]);
			map.put("itemName", row[1]);
			map.put("categoryName", row[2]);
			map.put("stockLevel", row[3]);
			map.put("unit", row[4]);
			result.add(map);
		}
		return result;
	}

	@Transactional
	public void createManualOrder(Integer branchId, Integer itemId, int amount) {
		ScmHqInventory manualOrder = new ScmHqInventory();
		manualOrder.setBranchId(branchId);
		manualOrder.setItemId(itemId);
		manualOrder.setApprovalStatus("PENDING");
		manualOrder.setDeliveryStatus("PREPARING");
		manualOrder.setRequestQuantity(amount);

		hqInventoryRepository.save(manualOrder);
	}

	public List<Map<String, Object>> getAllHqOrdersWithNames() {
		List<ScmHqInventory> rawOrders = hqInventoryRepository.findAll();

		Map<Integer, String> branchNameMap = new HashMap<>();
		branchRepository.findAll().forEach(b -> branchNameMap.put(b.getId(), b.getBranchName()));

		Map<Integer, String> itemNameMap = new HashMap<>();
		itemRepository.findAll().forEach(item -> itemNameMap.put(item.getId(), item.getItemName()));

		List<Map<String, Object>> result = new ArrayList<>();

		for (ScmHqInventory order : rawOrders) {
			Map<String, Object> map = new HashMap<>();
			map.put("hqInventoryId", order.getId());
			map.put("branchId", order.getBranchId());

			String branchName = branchNameMap.getOrDefault(order.getBranchId(), "미등록 지점(#" + order.getBranchId() + ")");
			map.put("branchName", branchName);

			map.put("itemId", order.getItemId());
			String itemName = itemNameMap.getOrDefault(order.getItemId(), "미등록 원자재(#" + order.getItemId() + ")");
			map.put("itemName", itemName);
			map.put("requestQuantity", order.getRequestQuantity());
			map.put("approvalStatus", order.getApprovalStatus());
			map.put("deliveryStatus", order.getDeliveryStatus());
			result.add(map);
		}
		return result;
	}
}