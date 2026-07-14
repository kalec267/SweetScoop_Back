package com.sweetscoop.inventory.repository;

import com.sweetscoop.inventory.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Integer> {
}