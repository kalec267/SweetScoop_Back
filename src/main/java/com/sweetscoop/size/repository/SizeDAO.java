package com.sweetscoop.size.repository;

import java.util.List;
import org.apache.ibatis.annotations.Param;

import com.sweetscoop.size.dto.SizeDTO;

public interface SizeDAO {

    List<SizeDTO> findByCategory(@Param("categoryId") Integer categoryId);

    SizeDTO findById(@Param("id") Integer id);

}