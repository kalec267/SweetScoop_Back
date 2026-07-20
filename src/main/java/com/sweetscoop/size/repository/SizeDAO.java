package com.sweetscoop.size.repository;

import java.util.List;

import org.springframework.data.repository.query.Param;

import com.sweetscoop.size.dto.SizeDTO;

public interface SizeDAO {

    List<SizeDTO> findByCategory(@Param("categoryId") Integer categoryId);

    SizeDTO findById(@Param("id") Integer id);

}