package com.al.auth.bean.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileVo {
    private Long userId;
    private String username;
    private String displayName;
    private List<String> roles;
    private List<String> permissions;
}
