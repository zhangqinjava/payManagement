package com.al.job;

import com.al.bean.vo.merchant.MerchantSettleConfigVo;
import com.al.common.Result;
import com.al.common.ResultEnum;
import com.al.common.exception.BusinessException;
import com.al.fegin.merchant.MerchantFeginClient;
import com.al.settle.dto.AccountSettleSnapshot;
import com.al.settle.entity.SettleRecordVo;
import com.al.settle.enums.SettleStatusEnum;
import com.al.settle.service.AccountSettleDataService;
import com.al.settle.service.SettleDetailService;
import com.al.settle.service.SettleRecordService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

/**
 * 结算任务：从账务模块抽取汇总与明细，生成结算单
 */
@Slf4j
@Component
public class SettleJob {

    private static final DateTimeFormatter SETTLE_NO_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter RECONCILE_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final SettleRecordService settleRecordService;
    private final SettleDetailService settleDetailService;
    private final AccountSettleDataService accountSettleDataService;
    private final SettleReconcileService settleReconcileService;
    private final SettleReconcileProperties settleReconcileProperties;
    private final MerchantFeginClient merchantFeginClient;

    @Autowired
    public SettleJob(SettleRecordService settleRecordService,
                     SettleDetailService settleDetailService,
                     AccountSettleDataService accountSettleDataService,
                     SettleReconcileService settleReconcileService,
                     SettleReconcileProperties settleReconcileProperties,
                     MerchantFeginClient merchantFeginClient) {
        this.settleRecordService = settleRecordService;
        this.settleDetailService = settleDetailService;
        this.accountSettleDataService = accountSettleDataService;
        this.settleReconcileService = settleReconcileService;
        this.settleReconcileProperties = settleReconcileProperties;
        this.merchantFeginClient = merchantFeginClient;
    }

    @XxlJob("settleDailyJob")
    public void settleDailyJob() {
        String param = XxlJobHelper.getJobParam();
        log.info("开始执行每日结算任务，参数：{}", param);
        try {
            LocalDate settleDate = LocalDate.now().minusDays(1);
            if (param != null && !param.trim().isEmpty()) {
                settleDate = LocalDate.parse(param.trim());
            }
            executeSettle(settleDate);
            XxlJobHelper.handleSuccess("结算任务执行成功");
        } catch (Exception e) {
            log.error("结算任务执行失败", e);
            XxlJobHelper.handleFail("结算任务执行失败：" + e.getMessage());
        }
    }

    @XxlJob("settleWeeklyJob")
    public void settleWeeklyJob() {
        log.info("开始执行周结任务");
        try {
            executeSettle(LocalDate.now().minusDays(1));
            XxlJobHelper.handleSuccess("周结任务执行成功");
        } catch (Exception e) {
            log.error("周结任务执行失败", e);
            XxlJobHelper.handleFail("周结任务执行失败：" + e.getMessage());
        }
    }

    @XxlJob("settleMonthlyJob")
    public void settleMonthlyJob() {
        log.info("开始执行月结任务");
        try {
            executeSettle(LocalDate.now().minusDays(1));
            XxlJobHelper.handleSuccess("月结任务执行成功");
        } catch (Exception e) {
            log.error("月结任务执行失败", e);
            XxlJobHelper.handleFail("月结任务执行失败：" + e.getMessage());
        }
    }

    private void executeSettle(LocalDate settleDate) throws Exception {
        log.info("结算日期：{}", settleDate);
        List<MerchantSettleConfigVo> configList = getActiveMerchantSettleConfigs();
        if (configList.isEmpty()) {
            log.info("没有有效的商户结算配置，任务结束");
            return;
        }
        log.info("共查询到{}个有效的商户结算配置", configList.size());

        int processedCount = 0;
        int skippedCount = 0;
        for (MerchantSettleConfigVo config : configList) {
            try {
                if (needSettle(config, settleDate)) {
                    processMerchantSettle(config, settleDate);
                    processedCount++;
                } else {
                    skippedCount++;
                }
            } catch (Exception e) {
                log.error("商户{}结算处理异常", config.getMerchantNo(), e);
            }
        }
        log.info("结算任务执行完成，已处理：{}个商户，跳过：{}个商户", processedCount, skippedCount);
    }

    private void processMerchantSettle(MerchantSettleConfigVo config, LocalDate settleDate) throws Exception {
        String merchantNo = config.getMerchantNo();
        String busiType = config.getBusiType();
        LocalDate[] period = calculateSettlePeriod(config.getSettleCycle(), settleDate, config.getSettleDelay());
        LocalDate startDate = period[0];
        LocalDate endDate = period[1];

        if (settleRecordService.existsRecord(merchantNo, busiType, startDate, endDate)) {
            log.info("商户{}周期{}~{}结算记录已存在，跳过", merchantNo, startDate, endDate);
            return;
        }

        if (settleReconcileProperties.isEnabled() && settleReconcileProperties.isRequired()) {
            String reconcileDate = endDate.format(RECONCILE_DATE);
            if (!settleReconcileService.hasPassedReconcile(merchantNo, reconcileDate)) {
                log.warn("商户{}在{}未完成对账或存在差异，跳过结算", merchantNo, reconcileDate);
                return;
            }
        }

        log.info("从账务侧抽取数据，商户号：{}，周期：{}~{}", merchantNo, startDate, endDate);
        AccountSettleSnapshot snapshot = accountSettleDataService.fetchSettleData(merchantNo, startDate, endDate);
        if (snapshot.getTransactionCount() == null || snapshot.getTransactionCount() == 0) {
            log.info("商户{}在周期{}~{}无账务流水，跳过结算", merchantNo, startDate, endDate);
            return;
        }
        if (snapshot.getNetAmount().compareTo(BigDecimal.ZERO) <= 0) {
            log.info("商户{}在周期{}~{}结算净额<=0，跳过结算", merchantNo, startDate, endDate);
            return;
        }

        String settleNo = generateSettleNo(merchantNo, settleDate);
        SettleRecordVo settleRecord = SettleRecordVo.builder()
                .settleNo(settleNo)
                .merchantNo(merchantNo)
                .busiType(busiType)
                .settleCycle(config.getSettleCycle())
                .settleStartDate(startDate)
                .settleEndDate(endDate)
                .settleDate(settleDate)
                .orderCount(snapshot.getTransactionCount())
                .totalAmount(snapshot.getTotalCredit())
                .totalFee(snapshot.getTotalFee())
                .netAmount(snapshot.getNetAmount())
                .settleStatus(SettleStatusEnum.PENDING.getCode())
                .payStatus(0)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        settleRecordService.save(settleRecord);
        settleDetailService.saveDetails(settleNo, snapshot.getDetails());
        log.info("商户{}结算记录生成成功，结算单号：{}，账务笔数：{}，收入：{}，支出：{}，净额：{}",
                merchantNo, settleNo, snapshot.getTransactionCount(),
                snapshot.getTotalCredit(), snapshot.getTotalFee(), snapshot.getNetAmount());
    }

    private List<MerchantSettleConfigVo> getActiveMerchantSettleConfigs() throws Exception {
        Result<List<MerchantSettleConfigVo>> result = merchantFeginClient.listActiveSettleConfigs();
        if (result == null || result.getCode() != ResultEnum.SUCESS.getCode()) {
            throw new BusinessException("查询商户结算配置失败");
        }
        return result.getData() == null ? Collections.emptyList() : result.getData();
    }

    private boolean needSettle(MerchantSettleConfigVo config, LocalDate settleDate) {
        String settleCycle = config.getSettleCycle();
        Integer settleDelay = config.getSettleDelay();
        switch (settleCycle) {
            case "DAY":
                return true;
            case "WEEK":
                int targetDayOfWeek = settleDelay != null ? settleDelay : 1;
                if (targetDayOfWeek < 1 || targetDayOfWeek > 7) {
                    targetDayOfWeek = 1;
                }
                return settleDate.getDayOfWeek().getValue() == targetDayOfWeek;
            case "MONTH":
                int targetDayOfMonth = settleDelay != null ? settleDelay : 1;
                if (targetDayOfMonth < 1 || targetDayOfMonth > 31) {
                    targetDayOfMonth = 1;
                }
                int effectiveDayOfMonth = Math.min(targetDayOfMonth, settleDate.lengthOfMonth());
                return settleDate.getDayOfMonth() == effectiveDayOfMonth;
            default:
                log.warn("未知的结算周期类型：{}，商户号：{}", settleCycle, config.getMerchantNo());
                return false;
        }
    }

    private LocalDate[] calculateSettlePeriod(String settleCycle, LocalDate settleDate, Integer settleDelay) {
        LocalDate targetDate = settleDate.minusDays(settleDelay != null ? settleDelay : 0);
        LocalDate startDate;
        LocalDate endDate;
        switch (settleCycle) {
            case "DAY":
                startDate = targetDate;
                endDate = targetDate;
                break;
            case "WEEK":
                startDate = targetDate.minusDays(6);
                endDate = targetDate;
                break;
            case "MONTH":
                startDate = targetDate.withDayOfMonth(1).minusMonths(1);
                endDate = startDate.plusMonths(1).minusDays(1);
                break;
            default:
                throw new IllegalArgumentException("不支持的结算周期类型：" + settleCycle);
        }
        return new LocalDate[]{startDate, endDate};
    }

    private String generateSettleNo(String merchantNo, LocalDate settleDate) {
        String dateStr = settleDate.format(SETTLE_NO_DATE);
        String merchantSuffix = merchantNo.length() > 4 ? merchantNo.substring(merchantNo.length() - 4) : merchantNo;
        String random = String.format("%04d", (int) (Math.random() * 10000));
        return "SETTLE" + dateStr + merchantSuffix + random;
    }
}
