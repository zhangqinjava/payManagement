package com.al.split.bean.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
public class OrderSplitRequestDto {
    @NotBlank(message = "订单号不能为空")
    private String orderNo;
    @NotBlank(message = "商户号不能为空")
    private String merchantNo;
    /** 自定义分账明细，为空则按默认规则（手续费+待清分） */
    @Valid
    private List<SplitReceiverDto> receivers;
}
