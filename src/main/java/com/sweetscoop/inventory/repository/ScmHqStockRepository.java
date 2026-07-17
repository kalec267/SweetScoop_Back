package com.sweetscoop.inventory.repository;

import com.sweetscoop.inventory.entity.ScmHqStock;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ScmHqStockRepository extends JpaRepository<ScmHqStock, Integer> {
    Optional<ScmHqStock> findByItemId(Integer itemId);
}