package com.al.settle.service.impl;

import com.al.bean.dto.reconcile.ReconcileExecuteDto;
import com.al.bean.dto.reconcile.ReconcileTaskQueryDto;
import com.al.bean.vo.account.AccountQueryDtlVo;
import com.al.bean.vo.reconcile.ReconcileExecuteResultVo;
import com.al.bean.vo.reconcile.ReconcileTaskVo;
import com.al.common.Result;
import com.al.common.ResultEnum;
import com.al.common.exception.BusinessException;
import com.al.config.ReconcileProperties;
import com.al.fegin.reconcile.ReconcileFeginClient;
import com.al.settle.dto.AccountSettleSnapshot;
import com.al.settle.service.AccountSettleDataService;
import com.al.settle.service.SettleReconcileService;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class SettleReconcileServiceImpl implements SettleReconcileService {

    @Autowired
    private AccountSettleDataService accountSettleDataService;
    @Autowired
    private ReconcileFeginClient reconcileFeginClient;
    @Autowired
    private ReconcileProperties reconcileProperties;

    @Override
    public List<Map<String, Object>> buildLocalRows(String merchantNo, LocalDate startDate, LocalDate endDate) throws Exception {
        AccountSettleSnapshot snapshot = accountSettleDataService.fetchSettleData(merchantNo, startDate, endDate);
        return convertDetails(snapshot.getDetails());
    }

    @Override
    public ReconcileExecuteResultVo execute(String reconcileDate, String channelCode, String merchantNo,
                                            String rawContent, String parseScriptCode, String compareScriptCode,
                                            LocalDate startDate, LocalDate endDate) throws Exception {
        if (StringUtils.isBlank(rawContent)) {
            throw new BusinessException("渠道对账原始数据不能为空");
        }
        ReconcileExecuteDto dto = new ReconcileExecuteDto();
        dto.setReconcileDate(reconcileDate);
        dto.setChannelCode(StringUtils.isNotBlank(channelCode) ? channelCode : reconcileProperties.getChannelCode());
        dto.setMerchantNo(merchantNo);
        dto.setParseScriptCode(StringUtils.isNotBlank(parseScriptCode)
                ? parseScriptCode : reconcileProperties.getParseScriptCode());
        dto.setCompareScriptCode(StringUtils.isNotBlank(compareScriptCode)
                ? compareScriptCode : reconcileProperties.getCompareScriptCode());
        dto.setRawContent(rawContent);
        dto.setLocalRows(buildLocalRows(merchantNo, startDate, endDate));

        Result<ReconcileExecuteResultVo> result = reconcileFeginClient.execute(dto);
        if (result == null || result.getCode() != ResultEnum.SUCESS.getCode() || result.getData() == null) {
            String msg = result != null ? result.getMsg() : "对账服务无响应";
            throw new BusinessException("对账执行失败:" + msg);
        }
        log.info("商户{}对账完成 taskNo={} diffCount={}", merchantNo, result.getData().getTaskNo(),
                result.getData().getDiffCount());
        return result.getData();
    }

    @Override
    public boolean hasPassedReconcile(String merchantNo, String reconcileDate) {
        ReconcileTaskQueryDto query = new ReconcileTaskQueryDto();
        query.setMerchantNo(merchantNo);
        query.setReconcileDate(reconcileDate);
        query.setStatus(1);
        try {
            Result<List<ReconcileTaskVo>> result = reconcileFeginClient.listTasks(query);
            if (result == null || result.getCode() != ResultEnum.SUCESS.getCode()
                    || CollectionUtils.isEmpty(result.getData())) {
                return false;
            }
            return result.getData().stream()
                    .anyMatch(task -> task.getDiffCount() != null && task.getDiffCount() == 0);
        } catch (Exception e) {
            log.warn("查询对账结果失败 merchantNo={}", merchantNo, e);
            return false;
        }
    }

    private List<Map<String, Object>> convertDetails(List<AccountQueryDtlVo> details) {
        List<Map<String, Object>> rows = new ArrayList<>();
        if (CollectionUtils.isEmpty(details)) {
            return rows;
        }
        for (AccountQueryDtlVo detail : details) {
            if (detail.getAmount() == null || detail.getAmount().compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }
            Map<String, Object> row = new HashMap<>();
            row.put("orderNo", resolveOrderNo(detail));
            row.put("amount", detail.getAmount());
            row.put("tradeDate", resolveTradeDate(detail));
            row.put("merchantNo", detail.getMerchantNo());
            row.put("flowNo", detail.getFlowNo());
            row.put("bizType", detail.getBizType());
            rows.add(row);
        }
        return rows;
    }

    private String resolveOrderNo(AccountQueryDtlVo detail) {
        if (StringUtils.isNotBlank(detail.getBizOrderNo())) {
            return detail.getBizOrderNo();
        }
        if (StringUtils.isNotBlank(detail.getFlowDtlNo())) {
            return detail.getFlowDtlNo();
        }
        return detail.getFlowNo();
    }

    private String resolveTradeDate(AccountQueryDtlVo detail) {
        if (StringUtils.isNotBlank(detail.getOrderDate())) {
            return detail.getOrderDate();
        }
        if (StringUtils.isNotBlank(detail.getBizOrderDate())) {
            return detail.getBizOrderDate();
        }
        return detail.getBizOrderTime();
    }
}
