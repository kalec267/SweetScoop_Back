package com.sweetscoop.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.sweetscoop.admin.entity.Promotion;

public interface PromotionRepository extends JpaRepository<Promotion, Integer> {

}