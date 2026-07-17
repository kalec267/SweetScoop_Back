package com.sweetscoop.admin.repository;

import com.sweetscoop.admin.entity.Size; // SIZE 테이블 엔티티
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SizeRepository extends JpaRepository<Size, Integer> {
}