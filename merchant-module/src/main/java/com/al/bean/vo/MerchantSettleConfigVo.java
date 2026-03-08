package com.al.bean.vo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@TableName("merchant_settle_config")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MerchantSettleConfigVo {

    private Long id;

    private String merchantNo;

    private String busiType;

    private String accountNo;

    private String accountType;

    private String settleCycle;

    private Integer settleDelay;

    private Integer status;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
