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
@TableName("t_set_account_dtl")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountDtlVo {
    @TableId(type = IdType.AUTO)
   private String id;
   private String  flowDtlNo;
   private String  storeId;
   private String  accountType;
   private String  flowNo;
   private String  bizType;
   private String  orderDate;
   private String  funCode;
   private BigDecimal amount;
   private BigDecimal curBalance;
   private String  fundDirection;
   private String  createTime;
   private String  updateTime;
}
