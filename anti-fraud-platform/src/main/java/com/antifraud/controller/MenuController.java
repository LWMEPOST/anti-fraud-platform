package com.antifraud.controller;

import com.antifraud.common.BusinessException;
import com.antifraud.common.ErrorCode;
import com.antifraud.common.Result;
import com.antifraud.dto.MenuDTO;
import com.antifraud.entity.SysUser;
import com.antifraud.mapper.UserMapper;
import com.antifraud.service.MenuService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.util.StringUtils;

import java.security.Principal;
import java.util.List;

@Api(tags = "菜单权限管理")
@RestController
@RequestMapping("/menu")
public class MenuController {

    @Autowired
    private MenuService menuService;

    @Autowired
    private UserMapper userMapper;

    @ApiOperation("获取当前登录用户的菜单权限")
    @GetMapping("/current")
    public Result<List<MenuDTO>> getCurrentUserMenus(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        List<MenuDTO> menus = menuService.getUserMenus(userId);
        return Result.success(menus);
    }

    @ApiOperation("根据用户ID获取菜单权限")
    @GetMapping("/user/{userId}")
    public Result<List<MenuDTO>> getUserMenus(@PathVariable Long userId) {
        List<MenuDTO> menus = menuService.getUserMenus(userId);
        return Result.success(menus);
    }

    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        Object details = authentication.getDetails();
        Long userIdFromDetails = parseUserId(details);
        if (userIdFromDetails != null) {
            return userIdFromDetails;
        }

        Long userIdFromPrincipal = extractUserIdFromPrincipal(authentication.getPrincipal());
        if (userIdFromPrincipal != null) {
            return userIdFromPrincipal;
        }

        if (StringUtils.hasText(authentication.getName())) {
            SysUser user = userMapper.selectOne(
                    new LambdaQueryWrapper<SysUser>()
                            .eq(SysUser::getUsername, authentication.getName())
                            .last("LIMIT 1")
            );
            if (user != null) {
                return user.getId();
            }
        }

        throw new BusinessException(ErrorCode.UNAUTHORIZED);
    }

    private Long extractUserIdFromPrincipal(Object principal) {
        if (principal instanceof Number) {
            return ((Number) principal).longValue();
        }

        if (principal instanceof Principal) {
            return parseUserId(((Principal) principal).getName());
        }

        return parseUserId(principal);
    }

    private Long parseUserId(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }

        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }

        return null;
    }
}
