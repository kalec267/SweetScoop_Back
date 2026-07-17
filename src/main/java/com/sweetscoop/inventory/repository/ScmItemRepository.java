package com.sweetscoop.inventory.repository;

import com.sweetscoop.inventory.entity.ScmItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScmItemRepository extends JpaRepository<ScmItem, Integer> {
}