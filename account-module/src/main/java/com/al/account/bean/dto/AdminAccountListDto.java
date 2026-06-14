package com.al.account.bean.dto;

import lombok.Data;

@Data
public class AdminAccountListDto {
    private Integer pageNum = 1;
    private Integer pageSize = 20;
    private String merchantNo;
    private String accountNo;
    private String accountType;
    private String accountStatus;
}
