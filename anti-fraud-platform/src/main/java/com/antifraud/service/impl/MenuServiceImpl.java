package com.antifraud.service.impl;

import com.antifraud.dto.MenuDTO;
import com.antifraud.entity.SysPermission;
import com.antifraud.entity.SysRolePermission;
import com.antifraud.entity.SysUserRole;
import com.antifraud.mapper.PermissionMapper;
import com.antifraud.mapper.RolePermissionMapper;
import com.antifraud.mapper.UserRoleMapper;
import com.antifraud.service.MenuService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MenuServiceImpl implements MenuService {

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    @Autowired
    private PermissionMapper permissionMapper;

    @Override
    public List<MenuDTO> getUserMenus(Long userId) {
        List<SysUserRole> userRoles = userRoleMapper.selectList(
            new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId)
        );

        if (userRoles.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> roleIds = userRoles.stream()
            .map(SysUserRole::getRoleId)
            .collect(Collectors.toList());

        List<SysRolePermission> rolePermissions = rolePermissionMapper.selectList(
            new LambdaQueryWrapper<SysRolePermission>()
                .in(SysRolePermission::getRoleId, roleIds)
        );

        if (rolePermissions.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> permissionIds = rolePermissions.stream()
            .map(SysRolePermission::getPermissionId)
            .distinct()
            .collect(Collectors.toList());

        List<SysPermission> permissions = permissionMapper.selectList(
            new LambdaQueryWrapper<SysPermission>()
                .in(SysPermission::getId, permissionIds)
                .eq(SysPermission::getStatus, 1)
                .orderByAsc(SysPermission::getSortOrder)
        );

        return buildMenuTree(includeParentPermissions(permissions));
    }

    private List<SysPermission> includeParentPermissions(List<SysPermission> permissions) {
        Map<Long, SysPermission> permissionMap = permissions.stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(
                SysPermission::getId,
                permission -> permission,
                (existing, replacement) -> existing,
                LinkedHashMap::new
            ));

        Set<Long> pendingParentIds = permissions.stream()
            .map(SysPermission::getParentId)
            .filter(parentId -> parentId != null && parentId > 0 && !permissionMap.containsKey(parentId))
            .collect(Collectors.toCollection(LinkedHashSet::new));

        while (!pendingParentIds.isEmpty()) {
            List<SysPermission> parentPermissions = permissionMapper.selectBatchIds(pendingParentIds).stream()
                .filter(Objects::nonNull)
                .filter(permission -> permission.getStatus() != null && permission.getStatus() == 1)
                .collect(Collectors.toList());

            for (SysPermission permission : parentPermissions) {
                permissionMap.putIfAbsent(permission.getId(), permission);
            }

            pendingParentIds = parentPermissions.stream()
                .map(SysPermission::getParentId)
                .filter(parentId -> parentId != null && parentId > 0 && !permissionMap.containsKey(parentId))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        }

        return permissionMap.values().stream()
            .sorted(Comparator.comparingInt(permission ->
                permission.getSortOrder() == null ? Integer.MAX_VALUE : permission.getSortOrder()
            ))
            .collect(Collectors.toList());
    }

    private List<MenuDTO> buildMenuTree(List<SysPermission> permissions) {
        Map<Long, MenuDTO> permissionMap = permissions.stream()
            .collect(Collectors.toMap(
                SysPermission::getId,
                permission -> {
                    MenuDTO dto = new MenuDTO();
                    dto.setId(permission.getId());
                    dto.setParentId(permission.getParentId());
                    dto.setPermissionName(permission.getPermissionName());
                    dto.setPermissionCode(permission.getPermissionCode());
                    dto.setMenuType(permission.getMenuType());
                    dto.setPath(permission.getPath());
                    dto.setComponent(permission.getComponent());
                    dto.setIcon(permission.getIcon());
                    dto.setChildren(new ArrayList<>());
                    return dto;
                },
                (existing, replacement) -> existing,
                LinkedHashMap::new
            ));

        List<MenuDTO> rootMenus = new ArrayList<>();

        for (MenuDTO menu : permissionMap.values()) {
            if (menu.getParentId() == null || menu.getParentId() == 0) {
                rootMenus.add(menu);
                continue;
            }

            MenuDTO parent = permissionMap.get(menu.getParentId());
            if (parent != null) {
                parent.getChildren().add(menu);
            }
        }

        return rootMenus;
    }
}
