package com.al.auth.aspect;

import com.al.auth.annotation.RequiresPermission;
import com.al.auth.security.LoginUser;
import com.al.auth.security.SecurityUtils;
import com.al.common.exception.BusinessException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PermissionAspect {

    @Before("@annotation(requiresPermission)")
    public void checkPermission(JoinPoint joinPoint, RequiresPermission requiresPermission) {
        LoginUser user = SecurityUtils.getLoginUser();
        if (user == null) {
            throw new BusinessException("未登录或登录已过期");
        }
        if (!user.hasPermission(requiresPermission.value())) {
            throw new BusinessException("无权限访问:" + requiresPermission.value());
        }
    }
}
