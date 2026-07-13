package com.sweetscoop.cup.repository;

import java.util.List;

import com.sweetscoop.cup.dto.CupDTO;

public interface CupDAO {

    List<CupDTO> findAll();

}
