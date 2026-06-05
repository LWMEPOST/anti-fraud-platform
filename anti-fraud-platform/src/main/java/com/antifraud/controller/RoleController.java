package com.antifraud.controller;

import com.antifraud.common.Result;
import com.antifraud.entity.SysRole;
import com.antifraud.mapper.RoleMapper;
import com.antifraud.service.RoleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Api(tags = "角色权限管理")
@RestController
@RequestMapping("/role")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @ApiOperation("获取角色列表")
    @GetMapping("/list")
    public Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<SysRole>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        return Result.success(roleService.getRoleList(pageNum, pageSize, keyword));
    }

    @Autowired
    private RoleMapper roleMapper;

    @ApiOperation("获取所有角色（下拉选择）")
    @GetMapping("/all")
    public Result<List<SysRole>> getAll() {
        return Result.success(roleMapper.selectList(null));
    }

    @ApiOperation("根据ID查询角色")
    @GetMapping("/{id}")
    public Result<SysRole> getById(@PathVariable Long id) {
        return Result.success(roleService.getRoleById(id));
    }

    @ApiOperation("新增角色")
    @PostMapping
    public Result<SysRole> add(@RequestBody SysRole role) {
        return Result.success(roleService.addRole(role));
    }

    @ApiOperation("更新角色")
    @PutMapping("/{id}")
    public Result<SysRole> update(@PathVariable Long id, @RequestBody SysRole role) {
        return Result.success(roleService.updateRole(id, role));
    }

    @ApiOperation("删除角色")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.deleteRole(id);
        return Result.success();
    }

    @ApiOperation("获取角色权限ID列表")
    @GetMapping("/{id}/permissions")
    public Result<List<Long>> getRolePermissions(@PathVariable Long id) {
        return Result.success(roleService.getRolePermissionIds(id));
    }

    @ApiOperation("分配权限（实时生效）")
    @PutMapping("/{id}/permissions")
    public Result<Void> assignPermissions(@PathVariable Long id, @RequestBody List<Long> permissionIds) {
        roleService.assignPermissions(id, permissionIds);
        return Result.success("权限分配成功，已实时生效");
    }

    @ApiOperation("获取权限树形结构")
    @GetMapping("/permission/tree")
    public Result<List<Map<String, Object>>> getPermissionTree() {
        return Result.success(roleService.getPermissionTree());
    }
}
