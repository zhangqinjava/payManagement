package com.al.reconcile.bean.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ReconcileScriptSaveDto {
    private Long id;
    @NotBlank(message = "脚本编码不能为空")
    private String scriptCode;
    @NotBlank(message = "脚本名称不能为空")
    private String scriptName;
    @NotBlank(message = "脚本类型不能为空")
    private String scriptType;
    private String channelCode;
    @NotBlank(message = "脚本内容不能为空")
    private String scriptContent;
    private Integer status;
    private String remark;
    private String createUser;
}
