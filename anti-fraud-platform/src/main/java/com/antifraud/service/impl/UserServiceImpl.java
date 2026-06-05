package com.antifraud.service.impl;

import com.antifraud.common.BusinessException;
import com.antifraud.common.ErrorCode;
import com.antifraud.dto.UserDTO;
import com.antifraud.entity.SysRole;
import com.antifraud.entity.SysUser;
import com.antifraud.entity.SysUserRole;
import com.antifraud.mapper.RoleMapper;
import com.antifraud.mapper.UserMapper;
import com.antifraud.mapper.UserRoleMapper;
import com.antifraud.security.CustomUserDetails;
import com.antifraud.service.UserService;
import com.antifraud.utils.JwtUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Map<String, Object> login(String username, String password) {
        try {
            UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(username, password);
            Authentication authentication = authenticationManager.authenticate(authenticationToken);

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            SysUser user = userDetails.getUser();
            user.setPassword(null);

            List<Long> roleIds = getUserRoleIds(user.getId());
            List<String> roleCodes = Collections.emptyList();
            if (!roleIds.isEmpty()) {
                roleCodes = roleMapper.selectBatchIds(roleIds).stream()
                        .filter(Objects::nonNull)
                        .filter(role -> role.getStatus() != null && role.getStatus() == 1)
                        .map(SysRole::getRoleCode)
                        .filter(StringUtils::hasText)
                        .distinct()
                        .collect(Collectors.toList());
            }

            String token = jwtUtil.generateToken(user.getId(), user.getUsername());

            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("user", user);
            result.put("roleCodes", roleCodes);
            return result;
        } catch (BadCredentialsException e) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        } catch (UsernameNotFoundException e) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        } catch (DisabledException e) {
            throw new BusinessException(ErrorCode.USER_DISABLED);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.ERROR);
        }
    }

    @Override
    @Transactional
    public SysUser register(UserDTO userDTO) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, userDTO.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.USER_EXIST);
        }

        SysUser user = new SysUser();
        LocalDateTime now = LocalDateTime.now();
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setRealName(userDTO.getRealName());
        user.setPhone(userDTO.getPhone());
        user.setEmail(userDTO.getEmail());
        user.setStatus(1);
        user.setCreateTime(now);
        user.setUpdateTime(now);
        userMapper.insert(user);

        if (userDTO.getRoleIds() != null && !userDTO.getRoleIds().isEmpty()) {
            assignRoles(user.getId(), userDTO.getRoleIds());
        } else {
            SysRole defaultRole = roleMapper.selectOne(
                    new LambdaQueryWrapper<SysRole>()
                            .eq(SysRole::getRoleCode, "USER")
                            .eq(SysRole::getStatus, 1)
                            .last("LIMIT 1")
            );
            if (defaultRole != null) {
                assignRoles(user.getId(), Collections.singletonList(defaultRole.getId()));
            }
        }

        user.setPassword(null);
        return user;
    }

    @Override
    public Page<SysUser> getUserList(int pageNum, int pageSize, String keyword, Integer status) {
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.like(SysUser::getUsername, keyword)
                   .or()
                   .like(SysUser::getRealName, keyword)
                   .or()
                   .like(SysUser::getPhone, keyword);
        }

        if (status != null) {
            wrapper.eq(SysUser::getStatus, status);
        }

        wrapper.orderByDesc(SysUser::getCreateTime);
        return userMapper.selectPage(page, wrapper);
    }

    @Override
    public SysUser getUserById(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    @Override
    @Transactional
    public SysUser updateUser(Long id, UserDTO userDTO) {
        SysUser user = getUserById(id);
        
        if (StringUtils.hasText(userDTO.getPassword())) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }
        if (userDTO.getRealName() != null) {
            user.setRealName(userDTO.getRealName());
        }
        if (userDTO.getPhone() != null) {
            user.setPhone(userDTO.getPhone());
        }
        if (userDTO.getEmail() != null) {
            user.setEmail(userDTO.getEmail());
        }
        if (userDTO.getStatus() != null) {
            user.setStatus(userDTO.getStatus());
        }

        userMapper.updateById(user);

        if (userDTO.getRoleIds() != null) {
            assignRoles(id, userDTO.getRoleIds());
        }

        return user;
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id));
        userMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteUsers(List<Long> ids) {
        for (Long id : ids) {
            userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id));
        }
        userMapper.deleteBatchIds(ids);
    }

    @Override
    public void resetPassword(Long id) {
        SysUser user = getUserById(id);
        user.setPassword(passwordEncoder.encode("123456"));
        userMapper.updateById(user);
    }

    @Override
    public void updateUserStatus(Long id, Integer status) {
        SysUser user = getUserById(id);
        user.setStatus(status);
        userMapper.updateById(user);
    }

    @Override
    public List<Long> getUserRoleIds(Long userId) {
        List<SysUserRole> userRoles = userRoleMapper.selectList(
            new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId)
        );
        return userRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void assignRoles(Long userId, List<Long> roleIds) {
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));

        for (Long roleId : roleIds) {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRoleMapper.insert(userRole);
        }
    }
}
