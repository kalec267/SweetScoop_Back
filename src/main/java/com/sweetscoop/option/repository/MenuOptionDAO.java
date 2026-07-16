package com.sweetscoop.option.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.sweetscoop.option.dto.MenuOptionDTO;

public interface MenuOptionDAO {

    List<MenuOptionDTO> findByCategoryId(
            @Param("categoryId") Integer categoryId
    );
}