package com.antifraud.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.antifraud.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PermissionMapper extends BaseMapper<SysPermission> {
}
