package com.sweetscoop.size.service;

import java.util.List;

import com.sweetscoop.size.dto.SizeDTO;

public interface SizeService {

	List<SizeDTO> getSizeList(Integer categoryId);

	SizeDTO getSize(Integer id);

}