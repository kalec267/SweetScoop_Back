package com.sweetscoop.size.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.sweetscoop.size.dto.SizeDTO;
import com.sweetscoop.size.repository.SizeDAO;

@Service
@RequiredArgsConstructor
public class SizeServiceImpl implements SizeService {

    private final SizeDAO sizeDAO;

    @Override
    public List<SizeDTO> getSizeList(Integer categoryId) {

        return sizeDAO.findByCategory(categoryId);

    }

    @Override
    public SizeDTO getSize(Integer id) {

        return sizeDAO.findById(id);

    }

}