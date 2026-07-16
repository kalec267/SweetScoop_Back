package com.sweetscoop.menu.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.sweetscoop.menu.dto.MenuDTO;
import com.sweetscoop.menu.repository.MenuDAO;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

	private final MenuDAO menuDAO;

	/*
	 * 전체 메뉴 조회
	 */
	@Override
	public List<MenuDTO> getMenuList() {

		return menuDAO.findAll();

	}

	/*
	 * 메뉴 상세 조회
	 */
	@Override
	public MenuDTO getMenu(Integer id) {

		return menuDAO.findById(id);

	}

	/*
	 * 카테고리 메뉴 조회
	 */
	@Override
	public List<MenuDTO> getMenuByCategory(Integer categoryId) {

		return menuDAO.findByCategory(categoryId);

	}

}