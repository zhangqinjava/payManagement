package com.al.split.bean.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("order_split_detail")
public class OrderSplitDetailVo {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String splitNo;
    private String detailNo;
    private String receiverMerchantNo;
    private String receiverAccountNo;
    private String receiverAccountType;
    private String splitType;
    private BigDecimal amount;
    private String flowNo;
    private Integer status;
    private String failReason;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
