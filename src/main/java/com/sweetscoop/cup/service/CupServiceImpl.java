package com.sweetscoop.cup.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sweetscoop.cup.dto.CupDTO;
import com.sweetscoop.cup.repository.CupDAO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CupServiceImpl implements CupService {

    private final CupDAO cupDAO;

    @Override
    public List<CupDTO> getCupList() {
        return cupDAO.findAll();
    }

}
