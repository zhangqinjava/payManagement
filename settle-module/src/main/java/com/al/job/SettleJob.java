package com.al.job;

import com.al.bean.vo.merchant.MerchantSettleConfigVo;
import com.al.settle.entity.SettleRecordVo;
import com.al.settle.enums.SettleStatusEnum;
import com.al.settle.service.SettleRecordService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
/**
 * 结算任务
 * 定时执行商户结算，生成结算记录
 * 调度策略：每日凌晨2点执行，结算前一天的订单
 */
@Slf4j
@Component
public class SettleJob {

    private final SettleRecordService settleRecordService;

    @Autowired
    public SettleJob(SettleRecordService settleRecordService) {
        this.settleRecordService = settleRecordService;
    }

    /**
     * 每日结算任务
     * 1. 查询所有有效的商户结算配置
     * 2. 根据结算周期（日结、周结、月结）和延迟天数，计算结算日期范围
     * 3. 查询商户在结算周期内的成功订单
     * 4. 计算总金额、手续费、结算净额
     * 5. 生成结算记录，状态为待结算
     * 6. 记录执行日志
     */
    @XxlJob("settleDailyJob")
    public void settleDailyJob() {
        // 获取XXL-JOB任务参数
        String param = XxlJobHelper.getJobParam();
        log.info("开始执行每日结算任务，参数：{}", param);

        try {
            // 默认结算日期为前一天
            LocalDate settleDate = LocalDate.now().minusDays(1);
            if (param != null && !param.trim().isEmpty()) {
                // 支持手动指定结算日期，格式：yyyy-MM-dd
                settleDate = LocalDate.parse(param.trim());
            }

            log.info("结算日期：{}", settleDate);

            // 1. 查询所有有效的商户结算配置
            // TODO: 需要实现商户结算配置Feign客户端
            List<MerchantSettleConfigVo> configList = getActiveMerchantSettleConfigs();

            if (configList.isEmpty()) {
                log.info("没有有效的商户结算配置，任务结束");
                XxlJobHelper.handleSuccess("没有有效的商户结算配置");
                return;
            }

            log.info("共查询到{}个有效的商户结算配置", configList.size());

            int processedCount = 0;
            int skippedCount = 0;

            for (MerchantSettleConfigVo config : configList) {
                try {
                    // 根据结算周期判断是否需要结算
                    if (needSettle(config, settleDate)) {
                        processMerchantSettle(config, settleDate);
                        processedCount++;
                    } else {
                        log.debug("商户{}在日期{}不需要结算（结算周期不匹配）", config.getMerchantNo(), settleDate);
                        skippedCount++;
                    }
                } catch (Exception e) {
                    log.error("商户{}结算处理异常", config.getMerchantNo(), e);
                    // 继续处理其他商户，不中断整个任务
                }
            }

            log.info("每日结算任务执行完成，已处理：{}个商户，跳过：{}个商户", processedCount, skippedCount);
            XxlJobHelper.handleSuccess("结算任务执行成功");
        } catch (Exception e) {
            log.error("结算任务执行失败", e);
            XxlJobHelper.handleFail("结算任务执行失败：" + e.getMessage());
        }
    }

    /**
     * 判断商户在指定日期是否需要结算
     * 结算逻辑：
     * 1. 日结（DAY）：每天结算，settleDelay表示延迟天数
     * 2. 周结（WEEK）：每周固定星期几结算，settleDelay表示星期几（1-7，1=周一）
     * 3. 月结（MONTH）：每月固定日期结算，settleDelay表示每月几号（1-31）
     */
    private boolean needSettle(MerchantSettleConfigVo config, LocalDate settleDate) {
        String settleCycle = config.getSettleCycle();
        Integer settleDelay = config.getSettleDelay();

        switch (settleCycle) {
            case "DAY":
                // 日结：每天都需要结算（考虑延迟）
                // 延迟逻辑在calculateSettlePeriod中处理
                return true;
            case "WEEK":
                // 周结：判断今天是否是配置的星期几
                // settleDelay: 1-7（1=周一，7=周日）
                int targetDayOfWeek = settleDelay != null ? settleDelay : 1;
                if (targetDayOfWeek < 1 || targetDayOfWeek > 7) {
                    log.warn("商户{}周结配置的星期几参数无效：{}，使用默认值1（周一）", config.getMerchantNo(), targetDayOfWeek);
                    targetDayOfWeek = 1;
                }
                int currentDayOfWeek = settleDate.getDayOfWeek().getValue();
                return currentDayOfWeek == targetDayOfWeek;
            case "MONTH":
                // 月结：判断今天是否是配置的每月几号
                // settleDelay: 1-31
                int targetDayOfMonth = settleDelay != null ? settleDelay : 1;
                if (targetDayOfMonth < 1 || targetDayOfMonth > 31) {
                    log.warn("商户{}月结配置的日期参数无效：{}，使用默认值1", config.getMerchantNo(), targetDayOfMonth);
                    targetDayOfMonth = 1;
                }
                int currentDayOfMonth = settleDate.getDayOfMonth();
                // 处理月末情况：如果当月天数小于配置的日期，则在最后一天结算
                int lastDayOfMonth = settleDate.lengthOfMonth();
                int effectiveDayOfMonth = Math.min(targetDayOfMonth, lastDayOfMonth);
                return currentDayOfMonth == effectiveDayOfMonth;
            default:
                log.warn("未知的结算周期类型：{}，商户号：{}", settleCycle, config.getMerchantNo());
                return false;
        }
    }

    /**
     * 处理单个商户的结算
     */
    private void processMerchantSettle(MerchantSettleConfigVo config, LocalDate settleDate) {
        String merchantNo = config.getMerchantNo();
        String busiType = config.getBusiType();
        String settleCycle = config.getSettleCycle();
        Integer settleDelay = config.getSettleDelay();

        log.info("开始处理商户结算，商户号：{}，业务类型：{}，结算日期：{}", merchantNo, busiType, settleDate);

        try {
            // 1. 计算结算周期起止时间
            LocalDate[] period = calculateSettlePeriod(settleCycle, settleDate, settleDelay);
            LocalDate startDate = period[0];
            LocalDate endDate = period[1];

            // 2. 查询商户在周期内的成功订单（tradeStatus=SUCCESS, accountStatus=SUCCESS）
            // TODO: 调用订单服务查询订单，这里先模拟数据
            List<OrderSettleInfo> orderList = querySettleOrders(merchantNo, busiType, startDate, endDate);

            if (orderList.isEmpty()) {
                log.info("商户{}在结算周期{}~{}内无成功订单，跳过结算", merchantNo, startDate, endDate);
                return;
            }

            // 3. 计算订单总金额、手续费、结算净额
            BigDecimal totalAmount = BigDecimal.ZERO;
            BigDecimal totalFee = BigDecimal.ZERO;
            int orderCount = 0;

            for (OrderSettleInfo order : orderList) {
                totalAmount = totalAmount.add(order.getAmount());
                totalFee = totalFee.add(order.getFee());
                orderCount++;
            }

            BigDecimal netAmount = totalAmount.subtract(totalFee);

            // 4. 生成结算记录（settle_record）
            SettleRecordVo settleRecord = SettleRecordVo.builder()
                    .settleNo(generateSettleNo(merchantNo, settleDate))
                    .merchantNo(merchantNo)
                    .busiType(busiType)
                    .settleCycle(settleCycle)
                    .settleStartDate(startDate)
                    .settleEndDate(endDate)
                    .settleDate(settleDate)
                    .orderCount(orderCount)
                    .totalAmount(totalAmount)
                    .totalFee(totalFee)
                    .netAmount(netAmount)
                    .settleStatus(SettleStatusEnum.PENDING.getCode())
                    .payStatus(0) // 待打款
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build();

            settleRecordService.save(settleRecord);
            log.info("商户{}结算记录生成成功，结算单号：{}，订单数：{}，结算净额：{}",
                    merchantNo, settleRecord.getSettleNo(), orderCount, netAmount);

            // 5. 更新订单的结算状态（可选）
            // TODO: 调用订单服务更新订单结算状态

            // 6. 记录结算明细
            // TODO: 记录结算明细表

        } catch (Exception e) {
            log.error("商户{}结算处理失败", merchantNo, e);
            throw e;
        }
    }

    /**
     * 周结任务（可选）
     * 每周一凌晨3点执行，结算上一周订单
     */
    @XxlJob("settleWeeklyJob")
    public void settleWeeklyJob() {
        log.info("开始执行周结任务");
        // 实现类似日结，但结算周期为一周
        XxlJobHelper.handleSuccess("周结任务执行成功");
    }

    /**
     * 月结任务（可选）
     * 每月1日凌晨4点执行，结算上一月订单
     */
    @XxlJob("settleMonthlyJob")
    public void settleMonthlyJob() {
        log.info("开始执行月结任务");
        // 实现类似日结，但结算周期为一月
        XxlJobHelper.handleSuccess("月结任务执行成功");
    }

    /**
     * 计算结算周期起止时间
     */
    private LocalDate[] calculateSettlePeriod(String settleCycle, LocalDate settleDate, Integer settleDelay) {
        LocalDate targetDate = settleDate.minusDays(settleDelay != null ? settleDelay : 0);
        LocalDate startDate;
        LocalDate endDate;

        switch (settleCycle) {
            case "DAY":
                // 日结：结算周期为前一天
                startDate = targetDate;
                endDate = targetDate;
                break;
            case "WEEK":
                // 周结：结算周期为上一周
                startDate = targetDate.minusDays(6); // 从上周一开始
                endDate = targetDate;
                break;
            case "MONTH":
                // 月结：结算周期为上一月
                startDate = targetDate.minusDays(targetDate.getDayOfMonth() - 1).minusMonths(1);
                endDate = startDate.plusMonths(1).minusDays(1);
                break;
            default:
                throw new IllegalArgumentException("不支持的结算周期类型：" + settleCycle);
        }

        return new LocalDate[]{startDate, endDate};
    }

    /**
     * 查询结算订单（模拟方法，实际需要调用订单服务）
     */
    private List<OrderSettleInfo> querySettleOrders(String merchantNo, String busiType, LocalDate startDate, LocalDate endDate) {
        // TODO: 调用订单服务查询商户在指定时间范围内的成功订单
        // 实际实现应该调用订单服务的Feign客户端
        log.info("查询商户{}业务类型{}在{}~{}期间的结算订单", merchantNo, busiType, startDate, endDate);
        // 返回空列表，实际应该查询数据库或调用服务
        return Collections.emptyList();
    }

    /**
     * 生成结算单号
     */
    private String generateSettleNo(String merchantNo, LocalDate settleDate) {
        // 格式：SETTLE + 日期(yyyyMMdd) + 商户号后4位 + 随机数4位
        String dateStr = settleDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String merchantSuffix = merchantNo.length() > 4 ? merchantNo.substring(merchantNo.length() - 4) : merchantNo;
        String random = String.format("%04d", (int)(Math.random() * 10000));
        return "SETTLE" + dateStr + merchantSuffix + random;
    }

    /**
     * 订单结算信息（内部类）
     */
    @Data
    @AllArgsConstructor
    private static class OrderSettleInfo {
        private String orderNo;
        private BigDecimal amount;
        private BigDecimal fee;
    }

    /**
     * 获取有效的商户结算配置（模拟方法）
     * TODO: 实现Feign客户端调用商户模块的接口
     */
    private List<MerchantSettleConfigVo> getActiveMerchantSettleConfigs() {
        // 模拟数据，实际应该调用商户模块的Feign客户端
        // 例如：return merchantSettleConfigFeignClient.listActiveConfigs();

        log.warn("使用模拟的商户结算配置数据，实际应该调用商户模块接口");

        // 创建一些测试配置
        return Collections.emptyList();
    }
}