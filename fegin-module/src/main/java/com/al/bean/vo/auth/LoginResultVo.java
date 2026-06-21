package com.al.bean.vo.auth;

import lombok.Data;

@Data
public class LoginResultVo {
    private String accessToken;
    private String refreshToken;
    private Long accessExpireAt;
    private Long refreshExpireAt;
    private UserProfileVo user;
}
