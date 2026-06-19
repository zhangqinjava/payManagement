package com.al.reconcile.bean.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

@Data
public class ReconcileExecuteDto {
    @NotBlank(message = "对账日期不能为空")
    private String reconcileDate;
    @NotBlank(message = "渠道编码不能为空")
    private String channelCode;
    @NotBlank(message = "解析脚本不能为空")
    private String parseScriptCode;
    @NotBlank(message = "比对脚本不能为空")
    private String compareScriptCode;
    private String merchantNo;
    @NotBlank(message = "渠道原始数据不能为空")
    private String rawContent;
    private List<Map<String, Object>> localRows;
}
