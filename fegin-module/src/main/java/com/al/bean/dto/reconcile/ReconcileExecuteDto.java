package com.al.bean.dto.reconcile;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ReconcileExecuteDto {
    private String reconcileDate;
    private String channelCode;
    private String parseScriptCode;
    private String compareScriptCode;
    private String merchantNo;
    private String rawContent;
    private List<Map<String, Object>> localRows;
}
