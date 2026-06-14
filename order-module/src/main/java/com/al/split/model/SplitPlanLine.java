package com.al.split.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class SplitPlanLine {
    private String receiverMerchantNo;
    private String receiverAccountNo;
    private String receiverAccountType;
    private BigDecimal amount;
    private String splitType;
    private String remark;
}
