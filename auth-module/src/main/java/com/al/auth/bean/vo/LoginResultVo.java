package com.al.auth.bean.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResultVo {
    private String accessToken;
    private String refreshToken;
    private Long accessExpireAt;
    private Long refreshExpireAt;
    private UserProfileVo user;
}
