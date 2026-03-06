package com.al.bean.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderTradeDto {
    @NotBlank(message = "商户号不能为空")
    private String merchantNo;
    @NotBlank(message = "订单号不能为空")
    @Max(32)
    @Min(1)
    private String orderNo;
    @NotBlank(message = "业务类型不能为空")
    private String bizType;
    @NotNull(message = "订单时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime orderDate;
    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.01", message = "金额必须大于0")
    private BigDecimal amount;
    @NotBlank(message = "付款通道不能为空")
    @Pattern(regexp = "^[12]$")
    private String payChannel;
    @NotBlank(message = "终端号不能为空")
    private String terminalNo;

}
