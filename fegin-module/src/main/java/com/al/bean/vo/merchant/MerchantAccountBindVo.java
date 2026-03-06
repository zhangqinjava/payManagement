package com.al.bean.vo.merchant;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("merchant_account_bind")
public class MerchantAccountBindVo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String merchantNo;

    private String accountNo;

    private Integer accountType;  // 1=结算账户, 2=保证金账户

    private Integer status;       // 1=启用, 0=停用

    private LocalDateTime bindTime;

    private LocalDateTime unbindTime;

    private String createUser;

    private String updateUser;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String remark;
}
