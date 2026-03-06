package com.al.bean.vo.account;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountOpenVo {
    private String flow;
    private String merchantNo;
    private String accountNo;
    private String accountType;
    private String accountStatus;
    private String remark;
    private String channelCode;
    private String channelAccountNo;
}
