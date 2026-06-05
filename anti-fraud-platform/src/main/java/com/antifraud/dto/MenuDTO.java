package com.antifraud.dto;

import lombok.Data;
import java.util.List;

@Data
public class MenuDTO {
    private Long id;
    private Long parentId;
    private String permissionName;
    private String permissionCode;
    private Integer menuType;
    private String path;
    private String component;
    private String icon;
    private List<MenuDTO> children;
}
