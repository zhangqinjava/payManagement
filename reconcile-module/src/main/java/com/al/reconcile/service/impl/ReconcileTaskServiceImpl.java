package com.al.reconcile.service.impl;

import com.al.common.exception.BusinessException;
import com.al.common.util.TraceUtil;
import com.al.reconcile.bean.dto.ReconcileExecuteDto;
import com.al.reconcile.bean.dto.ReconcileTaskQueryDto;
import com.al.reconcile.bean.vo.ReconcileDiffVo;
import com.al.reconcile.bean.vo.ReconcileExecuteResultVo;
import com.al.reconcile.bean.vo.ReconcileScriptVo;
import com.al.reconcile.bean.vo.ReconcileTaskVo;
import com.al.reconcile.engine.GroovyScriptRunner;
import com.al.reconcile.enums.TaskStatusEnum;
import com.al.reconcile.mapper.ReconcileDiffMapper;
import com.al.reconcile.mapper.ReconcileTaskMapper;
import com.al.reconcile.model.ReconcileContext;
import com.al.reconcile.service.ReconcileScriptService;
import com.al.reconcile.service.ReconcileTaskService;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ReconcileTaskServiceImpl implements ReconcileTaskService {

    @Autowired
    private ReconcileTaskMapper taskMapper;
    @Autowired
    private ReconcileDiffMapper diffMapper;
    @Autowired
    private ReconcileScriptService scriptService;
    @Autowired
    private GroovyScriptRunner groovyScriptRunner;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReconcileExecuteResultVo execute(ReconcileExecuteDto dto) {
        String taskNo = TraceUtil.createTraceId();
        LocalDateTime now = LocalDateTime.now();
        ReconcileTaskVo task = ReconcileTaskVo.builder()
                .taskNo(taskNo)
                .reconcileDate(dto.getReconcileDate())
                .channelCode(dto.getChannelCode())
                .parseScriptCode(dto.getParseScriptCode())
                .compareScriptCode(dto.getCompareScriptCode())
                .merchantNo(dto.getMerchantNo())
                .status(TaskStatusEnum.PROCESSING.getCode())
                .createTime(now)
                .build();
        taskMapper.insert(task);

        try {
            ReconcileScriptVo parseScript = scriptService.getActiveScript(dto.getParseScriptCode());
            ReconcileScriptVo compareScript = scriptService.getActiveScript(dto.getCompareScriptCode());
            ReconcileContext ctx = ReconcileContext.builder()
                    .reconcileDate(dto.getReconcileDate())
                    .channelCode(dto.getChannelCode())
                    .merchantNo(dto.getMerchantNo())
                    .taskNo(taskNo)
                    .build();

            List<Map<String, Object>> remoteRows = groovyScriptRunner.parse(
                    parseScript.getScriptCode(), parseScript.getVersion(), parseScript.getScriptContent(),
                    ctx, dto.getRawContent());
            List<Map<String, Object>> localRows = dto.getLocalRows() == null
                    ? Collections.emptyList() : dto.getLocalRows();
            List<Map<String, Object>> diffMaps = groovyScriptRunner.compare(
                    compareScript.getScriptCode(), compareScript.getVersion(), compareScript.getScriptContent(),
                    ctx, localRows, remoteRows);

            List<ReconcileDiffVo> diffs = saveDiffs(taskNo, diffMaps, now);
            task.setLocalCount(localRows.size());
            task.setRemoteCount(remoteRows.size());
            task.setDiffCount(diffs.size());
            task.setStatus(TaskStatusEnum.SUCCESS.getCode());
            task.setFinishTime(LocalDateTime.now());
            taskMapper.updateById(task);

            return ReconcileExecuteResultVo.builder()
                    .taskNo(taskNo)
                    .reconcileDate(dto.getReconcileDate())
                    .channelCode(dto.getChannelCode())
                    .status(task.getStatus())
                    .localCount(task.getLocalCount())
                    .remoteCount(task.getRemoteCount())
                    .diffCount(task.getDiffCount())
                    .diffs(diffs)
                    .build();
        } catch (Exception e) {
            log.error("reconcile task failed taskNo={}", taskNo, e);
            task.setStatus(TaskStatusEnum.FAILED.getCode());
            task.setErrorMsg(StringUtils.isNotBlank(e.getMessage()) ? e.getMessage() : "对账失败");
            task.setFinishTime(LocalDateTime.now());
            taskMapper.updateById(task);
            throw new BusinessException("对账执行失败:" + e.getMessage());
        }
    }

    @Override
    public ReconcileTaskVo queryTask(String taskNo) {
        ReconcileTaskVo task = taskMapper.selectOne(Wrappers.lambdaQuery(ReconcileTaskVo.class)
                .eq(ReconcileTaskVo::getTaskNo, taskNo));
        if (task == null) {
            throw new BusinessException("对账任务不存在:" + taskNo);
        }
        return task;
    }

    @Override
    public List<ReconcileDiffVo> queryDiffs(String taskNo) {
        return diffMapper.selectList(Wrappers.lambdaQuery(ReconcileDiffVo.class)
                .eq(ReconcileDiffVo::getTaskNo, taskNo)
                .orderByAsc(ReconcileDiffVo::getId));
    }

    @Override
    public List<ReconcileTaskVo> queryTasks(ReconcileTaskQueryDto dto) {
        return taskMapper.selectList(Wrappers.lambdaQuery(ReconcileTaskVo.class)
                .eq(StringUtils.isNotBlank(dto.getTaskNo()), ReconcileTaskVo::getTaskNo, dto.getTaskNo())
                .eq(StringUtils.isNotBlank(dto.getReconcileDate()), ReconcileTaskVo::getReconcileDate, dto.getReconcileDate())
                .eq(StringUtils.isNotBlank(dto.getChannelCode()), ReconcileTaskVo::getChannelCode, dto.getChannelCode())
                .eq(dto.getStatus() != null, ReconcileTaskVo::getStatus, dto.getStatus())
                .orderByDesc(ReconcileTaskVo::getCreateTime));
    }

    private List<ReconcileDiffVo> saveDiffs(String taskNo, List<Map<String, Object>> diffMaps, LocalDateTime now) {
        if (CollectionUtils.isEmpty(diffMaps)) {
            return Collections.emptyList();
        }
        List<ReconcileDiffVo> diffs = new ArrayList<>();
        for (Map<String, Object> diffMap : diffMaps) {
            ReconcileDiffVo diff = ReconcileDiffVo.builder()
                    .taskNo(taskNo)
                    .diffType(toString(diffMap.get("diffType")))
                    .bizKey(toString(diffMap.get("bizKey")))
                    .localAmount(toDecimal(diffMap.get("localAmount")))
                    .remoteAmount(toDecimal(diffMap.get("remoteAmount")))
                    .diffAmount(toDecimal(diffMap.get("diffAmount")))
                    .diffDetail(toString(diffMap.get("diffDetail")))
                    .status(0)
                    .createTime(now)
                    .build();
            diffMapper.insert(diff);
            diffs.add(diff);
        }
        return diffs;
    }

    private String toString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private BigDecimal toDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        return new BigDecimal(String.valueOf(value));
    }
}
