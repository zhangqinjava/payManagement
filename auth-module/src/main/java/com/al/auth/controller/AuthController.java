package com.al.auth.controller;

import com.al.auth.bean.dto.LoginDto;
import com.al.auth.bean.dto.RefreshTokenDto;
import com.al.auth.service.AuthService;
import com.al.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public Result login(@Valid @RequestBody LoginDto dto, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        return Result.success(authService.login(dto, ip));
    }

    @PostMapping("/logout")
    public Result logout(@RequestHeader(value = "X-Refresh-Token", required = false) String refreshToken) {
        authService.logout(refreshToken);
        return Result.success("退出成功");
    }

    @PostMapping("/refresh")
    public Result refresh(@Valid @RequestBody RefreshTokenDto dto) {
        return Result.success(authService.refresh(dto));
    }

    @GetMapping("/me")
    public Result me() {
        return Result.success(authService.currentUser());
    }
}
