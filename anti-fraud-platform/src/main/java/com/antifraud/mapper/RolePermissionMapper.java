package com.antifraud.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.antifraud.entity.SysRolePermission;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RolePermissionMapper extends BaseMapper<SysRolePermission> {
}
