package com.al.settle.service;

import com.al.bean.vo.reconcile.ReconcileExecuteResultVo;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface SettleReconcileService {
    List<Map<String, Object>> buildLocalRows(String merchantNo, LocalDate startDate, LocalDate endDate) throws Exception;

    ReconcileExecuteResultVo execute(String reconcileDate, String channelCode, String merchantNo,
                                     String rawContent, String parseScriptCode, String compareScriptCode,
                                     LocalDate startDate, LocalDate endDate) throws Exception;

    boolean hasPassedReconcile(String merchantNo, String reconcileDate);
}
