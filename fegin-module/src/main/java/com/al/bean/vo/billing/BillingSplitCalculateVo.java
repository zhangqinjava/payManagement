package com.al.bean.vo.billing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillingSplitCalculateVo {
    private String merchantNo;
    private String orderNo;
    private Integer bizType;
    private Integer feeMode;
    private Long ruleId;
    private BigDecimal amount;
    private BigDecimal feeAmount;
    private BigDecimal netAmount;
    private BigDecimal effectiveRate;
    private List<BillingTierDetailVo> tierDetails;
}
