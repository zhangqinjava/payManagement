package com.al.reconcile.bean.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ReconcileScriptTestDto {
    @NotBlank(message = "脚本内容不能为空")
    private String scriptContent;
    @NotBlank(message = "执行方法不能为空")
    private String methodName;
    private String reconcileDate;
    private String channelCode;
    private String merchantNo;
    private String rawContent;
    private String localDataJson;
    private String remoteDataJson;
}
