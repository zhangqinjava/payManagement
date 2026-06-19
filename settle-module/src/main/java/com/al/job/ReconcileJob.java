package com.al.job;

import cn.hutool.json.JSONUtil;
import com.al.bean.vo.merchant.MerchantSettleConfigVo;
import com.al.bean.vo.reconcile.ReconcileExecuteResultVo;
import com.al.common.Result;
import com.al.common.ResultEnum;
import com.al.common.exception.BusinessException;
import com.al.config.ReconcileProperties;
import com.al.fegin.merchant.MerchantFeginClient;
import com.al.settle.dto.ReconcileJobParam;
import com.al.settle.dto.ReconcileMerchantItem;
import com.al.settle.service.SettleReconcileService;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ReconcileJob {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Autowired
    private SettleReconcileService settleReconcileService;
    @Autowired
    private MerchantFeginClient merchantFeginClient;
    @Autowired
    private ReconcileProperties reconcileProperties;

    @XxlJob("reconcileDailyJob")
    public void reconcileDailyJob() {
        String param = XxlJobHelper.getJobParam();
        log.info("开始执行每日对账任务，参数：{}", param);
        try {
            ReconcileJobParam jobParam = parseParam(param);
            LocalDate reconcileDate = parseDate(jobParam.getReconcileDate());
            String dateStr = reconcileDate.format(DATE_FMT);
            Map<String, String> rawContentMap = toRawContentMap(jobParam.getItems());

            List<MerchantSettleConfigVo> configs = getActiveMerchantSettleConfigs();
            if (configs.isEmpty() && rawContentMap.isEmpty()) {
                log.info("无商户配置且无渠道数据，对账任务结束");
                XxlJobHelper.handleSuccess("无待对账数据");
                return;
            }

            int successCount = 0;
            int diffCount = 0;
            int skipCount = 0;
            for (MerchantSettleConfigVo config : configs) {
                String merchantNo = config.getMerchantNo();
                String rawContent = rawContentMap.get(merchantNo);
                if (StringUtils.isBlank(rawContent)) {
                    skipCount++;
                    log.info("商户{}未提供渠道原始数据，跳过对账", merchantNo);
                    continue;
                }
                try {
                    ReconcileExecuteResultVo result = settleReconcileService.execute(
                            dateStr,
                            jobParam.getChannelCode(),
                            merchantNo,
                            rawContent,
                            jobParam.getParseScriptCode(),
                            jobParam.getCompareScriptCode(),
                            reconcileDate,
                            reconcileDate);
                    successCount++;
                    if (result.getDiffCount() != null && result.getDiffCount() > 0) {
                        diffCount++;
                    }
                } catch (Exception e) {
                    log.error("商户{}对账失败", merchantNo, e);
                }
            }

            for (ReconcileMerchantItem item : jobParam.getItems() == null ? Collections.<ReconcileMerchantItem>emptyList() : jobParam.getItems()) {
                if (StringUtils.isBlank(item.getMerchantNo()) || StringUtils.isBlank(item.getRawContent())) {
                    continue;
                }
                if (configs.stream().anyMatch(c -> item.getMerchantNo().equals(c.getMerchantNo()))) {
                    continue;
                }
                try {
                    ReconcileExecuteResultVo result = settleReconcileService.execute(
                            dateStr,
                            jobParam.getChannelCode(),
                            item.getMerchantNo(),
                            item.getRawContent(),
                            jobParam.getParseScriptCode(),
                            jobParam.getCompareScriptCode(),
                            reconcileDate,
                            reconcileDate);
                    successCount++;
                    if (result.getDiffCount() != null && result.getDiffCount() > 0) {
                        diffCount++;
                    }
                } catch (Exception e) {
                    log.error("商户{}对账失败", item.getMerchantNo(), e);
                }
            }

            String message = String.format("对账完成，成功:%d，有差异:%d，跳过:%d", successCount, diffCount, skipCount);
            log.info(message);
            XxlJobHelper.handleSuccess(message);
        } catch (Exception e) {
            log.error("对账任务执行失败", e);
            XxlJobHelper.handleFail("对账任务执行失败：" + e.getMessage());
        }
    }

    private ReconcileJobParam parseParam(String param) {
        ReconcileJobParam jobParam = new ReconcileJobParam();
        jobParam.setChannelCode(reconcileProperties.getChannelCode());
        jobParam.setParseScriptCode(reconcileProperties.getParseScriptCode());
        jobParam.setCompareScriptCode(reconcileProperties.getCompareScriptCode());
        if (StringUtils.isBlank(param)) {
            jobParam.setReconcileDate(LocalDate.now().minusDays(1).format(DATE_FMT));
            return jobParam;
        }
        String trimmed = param.trim();
        if (trimmed.startsWith("{")) {
            ReconcileJobParam parsed = JSONUtil.toBean(trimmed, ReconcileJobParam.class);
            if (StringUtils.isBlank(parsed.getChannelCode())) {
                parsed.setChannelCode(reconcileProperties.getChannelCode());
            }
            if (StringUtils.isBlank(parsed.getParseScriptCode())) {
                parsed.setParseScriptCode(reconcileProperties.getParseScriptCode());
            }
            if (StringUtils.isBlank(parsed.getCompareScriptCode())) {
                parsed.setCompareScriptCode(reconcileProperties.getCompareScriptCode());
            }
            if (StringUtils.isBlank(parsed.getReconcileDate())) {
                parsed.setReconcileDate(LocalDate.now().minusDays(1).format(DATE_FMT));
            }
            return parsed;
        }
        jobParam.setReconcileDate(trimmed);
        return jobParam;
    }

    private LocalDate parseDate(String dateStr) {
        if (StringUtils.isBlank(dateStr)) {
            return LocalDate.now().minusDays(1);
        }
        if (dateStr.contains("-")) {
            return LocalDate.parse(dateStr);
        }
        return LocalDate.parse(dateStr, DATE_FMT);
    }

    private Map<String, String> toRawContentMap(List<ReconcileMerchantItem> items) {
        Map<String, String> map = new HashMap<>();
        if (CollectionUtils.isEmpty(items)) {
            return map;
        }
        for (ReconcileMerchantItem item : items) {
            if (StringUtils.isNotBlank(item.getMerchantNo()) && StringUtils.isNotBlank(item.getRawContent())) {
                map.put(item.getMerchantNo(), item.getRawContent());
            }
        }
        return map;
    }

    private List<MerchantSettleConfigVo> getActiveMerchantSettleConfigs() throws Exception {
        Result<List<MerchantSettleConfigVo>> result = merchantFeginClient.listActiveSettleConfigs();
        if (result == null || result.getCode() != ResultEnum.SUCESS.getCode()) {
            throw new BusinessException("查询商户结算配置失败");
        }
        return result.getData() == null ? Collections.emptyList() : result.getData();
    }
}
