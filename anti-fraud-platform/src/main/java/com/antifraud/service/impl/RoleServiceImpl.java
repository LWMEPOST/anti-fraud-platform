package com.antifraud.service.impl;

import com.antifraud.common.BusinessException;
import com.antifraud.common.ErrorCode;
import com.antifraud.entity.SysPermission;
import com.antifraud.entity.SysRole;
import com.antifraud.entity.SysRolePermission;
import com.antifraud.mapper.PermissionMapper;
import com.antifraud.mapper.RoleMapper;
import com.antifraud.mapper.RolePermissionMapper;
import com.antifraud.service.RoleService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private PermissionMapper permissionMapper;

    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    @Override
    public Page<SysRole> getRoleList(int pageNum, int pageSize, String keyword) {
        Page<SysRole> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.like(SysRole::getRoleName, keyword)
                   .or()
                   .like(SysRole::getRoleCode, keyword);
        }

        wrapper.orderByDesc(SysRole::getId);
        return roleMapper.selectPage(page, wrapper);
    }

    @Override
    public SysRole getRoleById(Long id) {
        return roleMapper.selectById(id);
    }

    @Override
    public SysRole addRole(SysRole role) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getRoleCode, role.getRoleCode());
        if (roleMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.ROLE_EXIST);
        }
        role.setCreateTime(LocalDateTime.now());
        roleMapper.insert(role);
        return role;
    }

    @Override
    public SysRole updateRole(Long id, SysRole role) {
        role.setId(id);
        roleMapper.updateById(role);
        return role;
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        rolePermissionMapper.delete(new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, id));
        roleMapper.deleteById(id);
    }

    @Override
    public List<Long> getRolePermissionIds(Long roleId) {
        List<SysRolePermission> rolePerms = rolePermissionMapper.selectList(
            new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, roleId)
        );
        return rolePerms.stream().map(SysRolePermission::getPermissionId).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        rolePermissionMapper.delete(new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, roleId));

        for (Long permId : permissionIds) {
            SysRolePermission rp = new SysRolePermission();
            rp.setRoleId(roleId);
            rp.setPermissionId(permId);
            rolePermissionMapper.insert(rp);
        }
    }

    @Override
    public List<Map<String, Object>> getPermissionTree() {
        List<SysPermission> allPerms = permissionMapper.selectList(
            new LambdaQueryWrapper<SysPermission>()
                .orderByAsc(SysPermission::getSortOrder)
        );

        Map<Long, Map<String, Object>> map = new HashMap<>();
        for (SysPermission perm : allPerms) {
            Map<String, Object> node = new HashMap<>();
            node.put("id", perm.getId());
            node.put("label", perm.getPermissionName());
            node.put("children", new ArrayList<>());
            map.put(perm.getId(), node);
        }

        List<Map<String, Object>> tree = new ArrayList<>();
        for (SysPermission perm : allPerms) {
            Map<String, Object> node = map.get(perm.getId());
            if (perm.getParentId() == 0) {
                tree.add(node);
            } else {
                Map<String, Object> parent = map.get(perm.getParentId());
                if (parent != null) {
                    ((List<Map<String, Object>>) parent.get("children")).add(node);
                }
            }
        }

        return tree;
    }
}
