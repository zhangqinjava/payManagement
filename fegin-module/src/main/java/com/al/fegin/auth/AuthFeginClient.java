package com.al.fegin.auth;

import com.al.bean.dto.auth.LoginDto;
import com.al.bean.dto.auth.RefreshTokenDto;
import com.al.bean.vo.auth.LoginResultVo;
import com.al.bean.vo.auth.UserProfileVo;
import com.al.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "auth-module", path = "/auth")
public interface AuthFeginClient {

    @PostMapping("/login")
    Result<LoginResultVo> login(@RequestBody LoginDto dto);

    @PostMapping("/refresh")
    Result<LoginResultVo> refresh(@RequestBody RefreshTokenDto dto);

    @PostMapping("/logout")
    Result<String> logout(@RequestHeader("X-Refresh-Token") String refreshToken);

    @GetMapping("/me")
    Result<UserProfileVo> me();
}
