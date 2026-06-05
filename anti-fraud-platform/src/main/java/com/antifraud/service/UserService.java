package com.antifraud.service;

import com.antifraud.dto.UserDTO;
import com.antifraud.entity.SysUser;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import java.util.Map;

public interface UserService {
    Map<String, Object> login(String username, String password);
    
    SysUser register(UserDTO userDTO);
    
    Page<SysUser> getUserList(int pageNum, int pageSize, String keyword, Integer status);
    
    SysUser getUserById(Long id);
    
    SysUser updateUser(Long id, UserDTO userDTO);
    
    void deleteUser(Long id);
    
    void deleteUsers(List<Long> ids);
    
    void resetPassword(Long id);
    
    void updateUserStatus(Long id, Integer status);
    
    List<Long> getUserRoleIds(Long userId);
    
    void assignRoles(Long userId, List<Long> roleIds);
}
