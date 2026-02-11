package com.al.account.bean.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_set_account")
public class AccountVo {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String storeId;
    private String accountNo;
    private String channelCode;
    private String channelAccountNo;
    private String accountType;
    private String currency;
    private BigDecimal balance;
    private BigDecimal frozenBalance;
    private BigDecimal transitBalance;
    private String accountStatus;
    private String remark;
    private String createTime;
    private String updateTime;
}
