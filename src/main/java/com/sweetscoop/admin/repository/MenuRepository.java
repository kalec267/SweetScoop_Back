package com.sweetscoop.admin.repository;

import com.sweetscoop.admin.entity.Menu; // 제공해주신 스키마의 MENU 테이블 엔티티
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Integer> {
    
    // 데이터베이스의 MENU 데이터를 한 번에 조회
    @Query("select m from Menu m order by m.id desc")
    List<Menu> findAllCustom();
}