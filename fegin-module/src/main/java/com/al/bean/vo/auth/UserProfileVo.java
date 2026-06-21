package com.al.bean.vo.auth;

import lombok.Data;

import java.util.List;

@Data
public class UserProfileVo {
    private Long userId;
    private String username;
    private String displayName;
    private List<String> roles;
    private List<String> permissions;
}
