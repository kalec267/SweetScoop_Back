package com.sweetscoop.kiosk.repository;

import com.sweetscoop.kiosk.entity.Kiosk;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface KioskRepository extends JpaRepository<Kiosk, Integer> {
    
    List<Kiosk> findByBranchId(Integer branchId);
}