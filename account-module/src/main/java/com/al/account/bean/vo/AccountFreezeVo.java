package com.al.account.bean.vo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_set_account_freeze")
public class AccountFreezeVo {
   private String  id;
   private String  freezeNo;
   private String  acountNo;
   private String  storeId;
   private String  accountType;
   private String  bizOrder;
   private String  bizType;
   private String  funCode;
   /**
    * 冻结状态 0-冻结中 2-冻结完成 3-冻结完毕
    */
   private String  freezeStatus;
   private String  freezeTotalBalance;
   private String  frezeeBalance;
   private String  unfreezeBalance;
   private String  remark;
   private String  createTime;
   private String  updateTime;
}
