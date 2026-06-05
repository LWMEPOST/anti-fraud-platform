package com.antifraud.service;

import com.antifraud.entity.SysRole;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import java.util.Map;

public interface RoleService {
    Page<SysRole> getRoleList(int pageNum, int pageSize, String keyword);
    
    SysRole getRoleById(Long id);
    
    SysRole addRole(SysRole role);
    
    SysRole updateRole(Long id, SysRole role);
    
    void deleteRole(Long id);
    
    List<Long> getRolePermissionIds(Long roleId);
    
    void assignPermissions(Long roleId, List<Long> permissionIds);
    
    List<Map<String, Object>> getPermissionTree();
}
