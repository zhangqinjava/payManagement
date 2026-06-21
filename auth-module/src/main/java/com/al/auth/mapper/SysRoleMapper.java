package com.al.auth.mapper;

import com.al.auth.bean.vo.SysRoleVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysRoleMapper extends BaseMapper<SysRoleVo> {

    @Select("SELECT p.perm_code FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON rp.permission_id = p.id " +
            "WHERE rp.role_id = #{roleId} AND p.status = 1")
    List<String> selectPermCodesByRoleId(@Param("roleId") Long roleId);
}
