package com.sweetscoop.admin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sweetscoop.admin.entity.Notice;

@Repository
public interface NoticeRepository 
        extends JpaRepository<Notice, Integer> {
	List<Notice> findAllByOrderByCreatedAtDesc();

}