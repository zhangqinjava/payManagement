package com.al.account.bean.vo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_set_account_freeze_detail")
public class AccountfreezeDetailVo {
   private String id;
   private String freezeDtlNo;
   private String freezeNo;
   private String storeId;
   private String accountNo;
   private String accountType;
   private String freezeBalance;
   private String bizOrder;
   private String bizType;
   private String createTime;
   private String updateTime;
}
