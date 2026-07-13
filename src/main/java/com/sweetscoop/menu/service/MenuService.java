package com.sweetscoop.menu.service;

import java.util.List;
import com.sweetscoop.menu.dto.MenuDTO;

public interface MenuService {

    List<MenuDTO> getMenuList();
    MenuDTO getMenu(Integer id);
    List<MenuDTO> getMenuByCategory(Integer categoryId);

}