package com.al.account.bean.dto;

import lombok.Data;

@Data
public class FreezeQueryDto {
    private String merchantNo;
    private String accountNo;
    private String accountType;
    private String freezeNo;
    /** 冻结状态 0-冻结中 2-冻结完成 3-冻结完毕 */
    private String freezeStatus;
    private String startDate;
    private String endDate;
}
