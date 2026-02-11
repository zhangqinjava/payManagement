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
   private String  flowNo;
   private String inAccountNo;
   private String  inStoreId;
   private String in_account_type;
   private String outStoreId;
   private String out_account_type;
   private String outAccountNo;
   private String bizType;
   private String bizOrderNo;
   private String bizOrderDate;
   private String bizOrderTime;
   private String orderDate;
   private BigDecimal amount;
   private String funCode;
   private String remark;
   private String createTime;
   private String updateTime;
}
