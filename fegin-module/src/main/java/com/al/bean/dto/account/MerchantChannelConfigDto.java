package com.al.bean.dto.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MerchantChannelConfigDto {
    @NotBlank(message = "通道编码不能为空")
    private String channelCode;
    @NotBlank(message = "通道名称不能为空")
    private String channelName;
    @NotBlank(message = "商户号不能为空")
    private String merchantNo;
    @NotNull(message = "权重不能为空")
    private Integer weight;
    @NotNull(message = "优先级不能为空")
    @Min(value = 1, message = "优先级必须大于0")
    @Max(value = 100, message = "优先级不能超过100")
    private Integer priority;
    private Integer status;
    @NotNull(message = "最大QPS不能为空")
    @Min(value = 1, message = "QPS必须大于0")
    @Max(value = 10000, message = "QPS不能超过10000")
    private Integer maxQps;
    @NotNull(message = "超时时间不能为空")
    @Min(value = 100, message = "超时时间最少100ms")
    @Max(value = 60000, message = "超时时间最大60000ms")
    private Integer timeoutMs;
}
