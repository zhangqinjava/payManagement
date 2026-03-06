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
@TableName("order_trade")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderTradeVo {
    private String tradeNo;
    private String merchantNo;
    private String orderNo;
    private LocalDateTime orderDate;
    private String bizType;
    private BigDecimal payAmount;
    private BigDecimal channelAmount;
    private String currency;
    private String payChannel;
    private Integer tradeStatus;
    private String failReason;
    private LocalDateTime requestTime;
    private LocalDateTime successTime;
    private Integer version;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
