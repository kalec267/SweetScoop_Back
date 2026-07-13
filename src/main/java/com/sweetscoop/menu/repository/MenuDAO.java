package com.sweetscoop.menu.repository;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.sweetscoop.menu.dto.MenuDTO;



public interface MenuDAO {


    List<MenuDTO> findAll();


    MenuDTO findById(Integer id);


    List<MenuDTO> findByCategory(@Param("categoryId") Integer categoryId);


}