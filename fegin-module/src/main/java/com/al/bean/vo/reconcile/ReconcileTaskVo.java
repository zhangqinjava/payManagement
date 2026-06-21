package com.al.bean.vo.reconcile;

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
public class ReconcileTaskVo {
    private Long id;
    private String taskNo;
    private String reconcileDate;
    private String channelCode;
    private String parseScriptCode;
    private String compareScriptCode;
    private String merchantNo;
    private Integer status;
    private Integer localCount;
    private Integer remoteCount;
    private Integer diffCount;
    private String errorMsg;
    private LocalDateTime createTime;
    private LocalDateTime finishTime;
}
