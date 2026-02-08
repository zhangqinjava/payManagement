package com.al.account.bean.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_set_account_open")
public class AccountOpenFlowVo {
    @TableId(type = IdType.AUTO)
   private String id;
   private String openOrderNo;
   private String accountNo;
   private String storeId;
   private String accountType;
   private String currency;
   private String channelCode;
   private String channelAccountNo;
   private String openStatus;
   private String failReason;
   private String operator;
   private String modifyUser;
   private String createTime;
   private String updateTime;
}
