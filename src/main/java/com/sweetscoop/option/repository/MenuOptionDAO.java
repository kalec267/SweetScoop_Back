package com.sweetscoop.option.repository;

import java.util.List;

import org.springframework.data.repository.query.Param;

import com.sweetscoop.option.dto.MenuOptionDTO;

public interface MenuOptionDAO {

    List<MenuOptionDTO> findByCategoryId(
            @Param("categoryId") Integer categoryId
    );
}