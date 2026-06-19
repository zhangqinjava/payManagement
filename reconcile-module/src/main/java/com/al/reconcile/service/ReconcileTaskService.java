package com.al.reconcile.service;

import com.al.reconcile.bean.dto.ReconcileExecuteDto;
import com.al.reconcile.bean.dto.ReconcileTaskQueryDto;
import com.al.reconcile.bean.vo.ReconcileDiffVo;
import com.al.reconcile.bean.vo.ReconcileExecuteResultVo;
import com.al.reconcile.bean.vo.ReconcileTaskVo;

import java.util.List;

public interface ReconcileTaskService {
    ReconcileExecuteResultVo execute(ReconcileExecuteDto dto);

    ReconcileTaskVo queryTask(String taskNo);

    List<ReconcileDiffVo> queryDiffs(String taskNo);

    List<ReconcileTaskVo> queryTasks(ReconcileTaskQueryDto dto);
}
