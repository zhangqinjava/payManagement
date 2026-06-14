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
@TableName("order_split_record")
public class OrderSplitRecordVo {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String splitNo;
    private String orderNo;
    private String tradeNo;
    private String merchantNo;
    private BigDecimal totalAmount;
    private BigDecimal feeAmount;
    private BigDecimal netAmount;
    private Integer splitStatus;
    private String failReason;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
