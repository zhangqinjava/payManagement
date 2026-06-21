package com.al.reconcile.service;

import com.al.reconcile.bean.dto.ReconcileScriptQueryDto;
import com.al.reconcile.bean.dto.ReconcileScriptSaveDto;
import com.al.reconcile.bean.dto.ReconcileScriptTestDto;
import com.al.reconcile.bean.vo.ReconcileScriptTestResultVo;
import com.al.reconcile.bean.vo.ReconcileScriptVo;

import java.util.List;

public interface ReconcileScriptService {
    ReconcileScriptVo save(ReconcileScriptSaveDto dto);

    List<ReconcileScriptVo> query(ReconcileScriptQueryDto dto);

    ReconcileScriptVo getActiveScript(String scriptCode);

    ReconcileScriptTestResultVo test(ReconcileScriptTestDto dto);
}
