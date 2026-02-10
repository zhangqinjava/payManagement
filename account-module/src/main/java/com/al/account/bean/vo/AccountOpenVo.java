package com.al.account.bean.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountOpenVo {
    private String flow;
    private String storeId;
    private String accountNo;
    private String accountType;
    private String accountStatus;
    private String remark;
    private String channelCode;
    private String channelAccountNo;
}
