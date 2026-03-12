package com.al.bean.vo;

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
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName(value = "order_refund_trade")
public class OrderrefundTradeVo {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String refundNo;
    private String orderNo;
    private String merchantNo;
    private String channel;
    private String channelNo;
    private BigDecimal refundAmount;
    private String refundReason;
    private Integer status;
    private String notifyStatus;
    private LocalDateTime refundTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
