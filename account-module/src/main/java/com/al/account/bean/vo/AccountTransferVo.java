package com.al.account.bean.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AccountTransferVo {
    private String inAccountNo;
    private String outAccountNo;
    private String inStoreId;
    private String outStoreId;
    private String inAccountType;
    private String outAccountType;
    private BigDecimal inCurBlance;
    private BigDecimal outCurBlance;
    private String bizOrderNo;
    private String bizOrderTime;
    private String bizOrderDate;
    private String bizType;
    private String funCode;
    private String amount;
    private String channelCode;

}
