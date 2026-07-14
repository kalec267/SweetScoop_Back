package com.sweetscoop.inventory.repository;

import com.sweetscoop.inventory.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BranchRepository extends JpaRepository<Branch, Integer> {
}