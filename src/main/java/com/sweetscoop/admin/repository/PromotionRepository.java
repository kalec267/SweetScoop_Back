package com.sweetscoop.admin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sweetscoop.admin.entity.Promotion;

public interface PromotionRepository extends JpaRepository<Promotion, Integer> {
	List<Promotion> findAllByOrderByIdDesc();
}