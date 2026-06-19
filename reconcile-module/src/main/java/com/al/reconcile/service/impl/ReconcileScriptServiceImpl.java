package com.al.reconcile.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.al.common.exception.BusinessException;
import com.al.reconcile.bean.dto.ReconcileScriptQueryDto;
import com.al.reconcile.bean.dto.ReconcileScriptSaveDto;
import com.al.reconcile.bean.dto.ReconcileScriptTestDto;
import com.al.reconcile.bean.vo.ReconcileScriptTestResultVo;
import com.al.reconcile.bean.vo.ReconcileScriptVo;
import com.al.reconcile.engine.GroovyScriptRunner;
import com.al.reconcile.mapper.ReconcileScriptMapper;
import com.al.reconcile.model.ReconcileContext;
import com.al.reconcile.service.ReconcileScriptService;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class ReconcileScriptServiceImpl implements ReconcileScriptService {

    private static final int STATUS_ACTIVE = 1;

    @Autowired
    private ReconcileScriptMapper scriptMapper;
    @Autowired
    private GroovyScriptRunner groovyScriptRunner;

    @Override
    public ReconcileScriptVo save(ReconcileScriptSaveDto dto) {
        groovyScriptRunner.validate(dto.getScriptContent());
        LocalDateTime now = LocalDateTime.now();
        ReconcileScriptVo exist = scriptMapper.selectOne(Wrappers.lambdaQuery(ReconcileScriptVo.class)
                .eq(ReconcileScriptVo::getScriptCode, dto.getScriptCode()));
        if (exist == null) {
            ReconcileScriptVo script = ReconcileScriptVo.builder()
                    .scriptCode(dto.getScriptCode())
                    .scriptName(dto.getScriptName())
                    .scriptType(dto.getScriptType())
                    .channelCode(StringUtils.isNotBlank(dto.getChannelCode()) ? dto.getChannelCode() : "DEFAULT")
                    .scriptContent(dto.getScriptContent())
                    .version(1)
                    .status(dto.getStatus() == null ? STATUS_ACTIVE : dto.getStatus())
                    .remark(dto.getRemark())
                    .createUser(StringUtils.isNotBlank(dto.getCreateUser()) ? dto.getCreateUser() : "system")
                    .updateUser(StringUtils.isNotBlank(dto.getCreateUser()) ? dto.getCreateUser() : "system")
                    .createTime(now)
                    .updateTime(now)
                    .build();
            scriptMapper.insert(script);
            return script;
        }
        exist.setScriptName(dto.getScriptName());
        exist.setScriptType(dto.getScriptType());
        exist.setChannelCode(StringUtils.isNotBlank(dto.getChannelCode()) ? dto.getChannelCode() : exist.getChannelCode());
        exist.setScriptContent(dto.getScriptContent());
        exist.setVersion(exist.getVersion() + 1);
        exist.setStatus(dto.getStatus() == null ? exist.getStatus() : dto.getStatus());
        exist.setRemark(dto.getRemark());
        exist.setUpdateUser(StringUtils.isNotBlank(dto.getCreateUser()) ? dto.getCreateUser() : "system");
        exist.setUpdateTime(now);
        scriptMapper.updateById(exist);
        groovyScriptRunner.invalidate(exist.getScriptCode());
        return exist;
    }

    @Override
    public List<ReconcileScriptVo> query(ReconcileScriptQueryDto dto) {
        return scriptMapper.selectList(Wrappers.lambdaQuery(ReconcileScriptVo.class)
                .eq(StringUtils.isNotBlank(dto.getScriptCode()), ReconcileScriptVo::getScriptCode, dto.getScriptCode())
                .eq(StringUtils.isNotBlank(dto.getScriptType()), ReconcileScriptVo::getScriptType, dto.getScriptType())
                .eq(StringUtils.isNotBlank(dto.getChannelCode()), ReconcileScriptVo::getChannelCode, dto.getChannelCode())
                .eq(dto.getStatus() != null, ReconcileScriptVo::getStatus, dto.getStatus())
                .orderByDesc(ReconcileScriptVo::getUpdateTime));
    }

    @Override
    public ReconcileScriptVo getActiveScript(String scriptCode) {
        ReconcileScriptVo script = scriptMapper.selectOne(Wrappers.lambdaQuery(ReconcileScriptVo.class)
                .eq(ReconcileScriptVo::getScriptCode, scriptCode)
                .eq(ReconcileScriptVo::getStatus, STATUS_ACTIVE));
        if (script == null) {
            throw new BusinessException("脚本不存在或已停用:" + scriptCode);
        }
        return script;
    }

    @Override
    public ReconcileScriptTestResultVo test(ReconcileScriptTestDto dto) {
        groovyScriptRunner.validate(dto.getScriptContent());
        ReconcileContext ctx = buildContext(dto.getReconcileDate(), dto.getChannelCode(), dto.getMerchantNo(), "TEST");
        Object result;
        if ("parse".equalsIgnoreCase(dto.getMethodName())) {
            result = groovyScriptRunner.parse("TEST", 1, dto.getScriptContent(), ctx, dto.getRawContent());
        } else if ("compare".equalsIgnoreCase(dto.getMethodName())) {
            List<Map<String, Object>> localRows = parseJsonRows(dto.getLocalDataJson());
            List<Map<String, Object>> remoteRows = parseJsonRows(dto.getRemoteDataJson());
            result = groovyScriptRunner.compare("TEST", 1, dto.getScriptContent(), ctx, localRows, remoteRows);
        } else {
            result = groovyScriptRunner.invoke("TEST", 1, dto.getScriptContent(), dto.getMethodName(), ctx, ctx);
        }
        return ReconcileScriptTestResultVo.builder()
                .methodName(dto.getMethodName())
                .result(result)
                .message("执行成功")
                .build();
    }

    private ReconcileContext buildContext(String reconcileDate, String channelCode, String merchantNo, String taskNo) {
        return ReconcileContext.builder()
                .reconcileDate(reconcileDate)
                .channelCode(StringUtils.isNotBlank(channelCode) ? channelCode : "DEFAULT")
                .merchantNo(merchantNo)
                .taskNo(taskNo)
                .build();
    }

    private List<Map<String, Object>> parseJsonRows(String json) {
        if (StringUtils.isBlank(json)) {
            return Collections.emptyList();
        }
        return JSON.parseObject(json, new TypeReference<List<Map<String, Object>>>() {
        });
    }
}
