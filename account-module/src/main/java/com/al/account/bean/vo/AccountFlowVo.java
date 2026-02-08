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
@TableName("t_set_account_flow")
public class AccountFlowVo {
   @TableId(type = IdType.AUTO)
   private String  id;;
   /**
    * 账户号
    */
   private String  accountNo;
   private String  storeId;
   private String  flowNo;
   private String  bizType;
   private String  bizOrderNo;
   private BigDecimal amount;
   private BigDecimal  curBalance;
   private String  fundDirection;
   private String  funCode;
   private String  createTime;
   private String  updateTime;
}
