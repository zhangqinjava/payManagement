package com.al.auth.security;

import com.al.auth.bean.vo.SysUserVo;
import com.al.auth.mapper.SysUserMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LoginUserDetailsService implements UserDetailsService {

    @Autowired
    private SysUserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUserVo user = userMapper.selectOne(Wrappers.lambdaQuery(SysUserVo.class)
                .eq(SysUserVo::getUsername, username));
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        return buildLoginUser(user);
    }

    public LoginUser buildLoginUser(SysUserVo user) {
        List<String> roles = userMapper.selectRolesByUserId(user.getId()).stream()
                .map(r -> r.getRoleCode())
                .collect(Collectors.toList());
        List<String> permissions = userMapper.selectPermissionsByUserId(user.getId()).stream()
                .map(p -> p.getPermCode())
                .collect(Collectors.toList());
        boolean enabled = user.getStatus() != null && user.getStatus() == 1;
        return new LoginUser(user.getId(), user.getUsername(), user.getPassword(),
                user.getDisplayName(), enabled, roles, permissions);
    }
}
