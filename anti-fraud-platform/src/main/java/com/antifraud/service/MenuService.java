package com.antifraud.service;

import com.antifraud.dto.MenuDTO;
import java.util.List;

public interface MenuService {
    List<MenuDTO> getUserMenus(Long userId);
}
