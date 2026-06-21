package com.al.auth.service;

import com.al.auth.bean.dto.LoginDto;
import com.al.auth.bean.dto.RefreshTokenDto;
import com.al.auth.bean.vo.LoginResultVo;
import com.al.auth.bean.vo.UserProfileVo;

public interface AuthService {
    LoginResultVo login(LoginDto dto, String clientIp);

    void logout(String refreshToken);

    LoginResultVo refresh(RefreshTokenDto dto);

    UserProfileVo currentUser();
}
