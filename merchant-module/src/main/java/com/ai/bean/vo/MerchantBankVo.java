package com.ai.bean.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@TableName("merchant_bank_card")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MerchantBankVo {
    @TableId(type = IdType.AUTO)
    private String id;
    private String merchantId;
    private String cardType;
    private String bankCode;
    private String bankName;
    private String cardNoEncrypt;
    private String cardNoMask;
    private String cardMd5;
    private String cardName;
    private String idCardEncrypt;
    private String idCardType;
    private String mobileEncrypt;
    private Integer isDefault;
    private Integer bindStatus;
    private String bindTime;
    private String unbindTime;
    private String remark;
    private String createdTime;
    private String updatedTime;
}
