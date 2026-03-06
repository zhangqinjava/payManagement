package com.al.bean.vo.merchant;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@TableName("merchant_terminal_bind")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MerchantTerminalBindVo {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String merchantNo;       // 商户号

    private String terminalNo;       // 终端号

    private String terminalType;     // 终端类型

    private Integer status;          // 状态 1=启用 0=停用

    private LocalDateTime bindTime;

    private LocalDateTime unbindTime;

    private String createUser;

    private String updateUser;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String remark;
}
