package com.al.auth.mapper;

import com.al.auth.bean.vo.SysPermissionVo;
import com.al.auth.bean.vo.SysRoleVo;
import com.al.auth.bean.vo.SysUserVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUserVo> {

    @Select("SELECT r.* FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON ur.role_id = r.id " +
            "WHERE ur.user_id = #{userId} AND r.status = 1")
    List<SysRoleVo> selectRolesByUserId(@Param("userId") Long userId);

    @Select("SELECT DISTINCT p.* FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON rp.permission_id = p.id " +
            "INNER JOIN sys_user_role ur ON ur.role_id = rp.role_id " +
            "WHERE ur.user_id = #{userId} AND p.status = 1")
    List<SysPermissionVo> selectPermissionsByUserId(@Param("userId") Long userId);
}
