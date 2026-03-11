package com.al.bean.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.bytebuddy.asm.Advice;

import java.time.LocalDateTime;

@TableName("merchant_bank_card")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MerchantBankVo {
    @TableId(type = IdType.AUTO)
    private String id;
    private String merchantNo;
    private Integer cardType;
    private String bankCode;
    private String bankName;
    private String cardNoEncrypt;
    private String cardNoMask;
    private String cardName;
    private String idCardEncrypt;
    private String idCardType;
    private String mobileEncrypt;
    private Integer isDefault;
    private Integer bindStatus;
    private LocalDateTime bindTime;
    private LocalDateTime unbindTime;
    private String remark;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
