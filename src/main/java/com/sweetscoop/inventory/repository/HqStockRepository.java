package com.sweetscoop.inventory.repository;

import com.sweetscoop.inventory.entity.HqStock;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface HqStockRepository extends JpaRepository<HqStock, Integer> {
    Optional<HqStock> findByItemId(Integer itemId);
}