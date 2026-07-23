package com.sweetscoop.admin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sweetscoop.admin.entity.CS;

public interface CSRepository extends JpaRepository<CS, Integer> {
	List<CS> findAllByOrderByCreatedAtDesc();
}