package com.al.reconcile.bean.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReconcileScriptTestResultVo {
    private String methodName;
    private Object result;
    private String message;
}
