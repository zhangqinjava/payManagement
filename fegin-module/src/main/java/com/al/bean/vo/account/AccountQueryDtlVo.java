package com.al.bean.vo.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountQueryDtlVo {
    private String id;
    private String  flowDtlNo;
    private String  merchantNo;
    private String  accountType;
    private String  flowNo;
    private String  bizType;
    private String  orderDate;
    private String  funCode;
    private BigDecimal amount;
    private BigDecimal curBalance;
    private String  fundDirection;
    private String inAccountNo;
    private String  inMerchantNo;
    private String inAccountType;
    private String outMerchantNo;
    private String out_account_type;
    private String outAccountNo;
    private String bizOrderNo;
    private String bizOrderDate;
    private String bizOrderTime;
    private String remark;
}
