package com.al.auth.config;

import com.al.auth.bean.vo.SysRoleVo;
import com.al.auth.bean.vo.SysUserVo;
import com.al.auth.mapper.SysRoleMapper;
import com.al.auth.mapper.SysUserMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Slf4j
@Component
public class AuthDataInitializer implements CommandLineRunner {

    private static final String DEFAULT_ADMIN = "admin";
    private static final String DEFAULT_PASSWORD = "admin123";

    @Autowired
    private SysUserMapper userMapper;
    @Autowired
    private SysRoleMapper roleMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        if (userMapper.selectCount(Wrappers.lambdaQuery(SysUserVo.class)) > 0) {
            return;
        }
        log.info("init default admin user and rbac data");
        SysUserVo admin = new SysUserVo();
        admin.setUsername(DEFAULT_ADMIN);
        admin.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        admin.setDisplayName("系统管理员");
        admin.setStatus(1);
        admin.setLoginFailCount(0);
        admin.setAllowedStartTime(LocalTime.of(8, 0));
        admin.setAllowedEndTime(LocalTime.of(22, 0));
        admin.setCreateTime(LocalDateTime.now());
        admin.setUpdateTime(LocalDateTime.now());
        userMapper.insert(admin);

        SysRoleVo adminRole = roleMapper.selectOne(Wrappers.lambdaQuery(SysRoleVo.class)
                .eq(SysRoleVo::getRoleCode, "ADMIN"));
        if (adminRole != null) {
            jdbcTemplate.update("INSERT INTO sys_user_role(user_id, role_id) VALUES (?, ?)",
                    admin.getId(), adminRole.getId());
            jdbcTemplate.update("INSERT INTO sys_role_permission(role_id, permission_id) " +
                            "SELECT ?, p.id FROM sys_permission p WHERE p.status = 1",
                    adminRole.getId());
        }
        log.info("default admin created, username={}, password={}", DEFAULT_ADMIN, DEFAULT_PASSWORD);
    }
}
