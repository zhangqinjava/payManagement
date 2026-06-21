package com.al.auth.controller;

import com.al.auth.annotation.RequiresPermission;
import com.al.auth.bean.vo.SysUserVo;
import com.al.auth.mapper.SysUserMapper;
import com.al.common.result.Result;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user")
public class SysUserController {

    @Autowired
    private SysUserMapper userMapper;

    @GetMapping("/list")
    @RequiresPermission("system:user:query")
    public Result listUsers() {
        List<SysUserVo> users = userMapper.selectList(Wrappers.lambdaQuery(SysUserVo.class)
                .select(SysUserVo::getId, SysUserVo::getUsername, SysUserVo::getDisplayName,
                        SysUserVo::getStatus, SysUserVo::getLastLoginTime,
                        SysUserVo::getAllowedStartTime, SysUserVo::getAllowedEndTime,
                        SysUserVo::getLockUntil)
                .orderByDesc(SysUserVo::getId));
        users.forEach(u -> u.setPassword(null));
        return Result.success(users);
    }
}
