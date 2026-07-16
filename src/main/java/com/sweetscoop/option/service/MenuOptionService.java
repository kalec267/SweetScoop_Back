package com.sweetscoop.option.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sweetscoop.option.dto.MenuOptionDTO;
import com.sweetscoop.option.repository.MenuOptionDAO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MenuOptionService {

    private final MenuOptionDAO menuOptionDAO;

    public List<MenuOptionDTO> getOptionsByCategory(
            Integer categoryId) {

        return menuOptionDAO.findByCategoryId(categoryId);
    }
}