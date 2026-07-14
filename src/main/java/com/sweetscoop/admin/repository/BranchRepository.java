package com.sweetscoop.admin.repository;

import com.sweetscoop.admin.entity.Branch; // 기존에 작성한 BRANCH 엔티티
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Integer> {
    // 기본적인 count(), findAll(), findById() 등은 JpaRepository가 자동으로 제공합니다.
}