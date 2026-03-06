package com.al.service.impl.order;

import com.al.bean.dto.OrderTradeDto;
import com.al.bean.dto.merchant.MerchantFeeDto;
import com.al.bean.vo.OrderTradeVo;
import com.al.bean.vo.merchant.MerchantVo;
import com.al.common.Result;
import com.al.common.ResultEnum;
import com.al.common.business.BusiEnum;
import com.al.common.business.Const;
import com.al.common.business.MerchantEnum;
import com.al.common.exception.BusinessException;
import com.al.fegin.merchant.MerchantFeginClient;
import com.al.mapper.OrderTradeMapper;
import com.al.service.order.OrderService;
import com.al.service.order.OrderTradeService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    @Autowired
    private MerchantFeginClient merchantFeginClient;
    @Resource(name = "asyncThreadConfig")
    private ThreadPoolExecutor threadPoolExecutor;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private OrderTradeMapper orderTradeMapper;
    @Autowired
    private OrderTradeService orderTradeService;
    @Override
    public OrderTradeVo create(OrderTradeDto orderTradeDto) throws Exception {
        RLock  lock=null;
        try{
            log.info("order create event start request param:{]", orderTradeDto);
             lock = redissonClient.getLock(Const.ORDER_LOCK+orderTradeDto.getOrderNo());
             //幂等性校验
             if (lock.tryLock()) {
                 OrderTradeVo exists = orderTradeMapper.selectOne(
                         Wrappers.<OrderTradeVo>lambdaQuery()
                                 .eq(OrderTradeVo::getMerchantNo, orderTradeDto.getMerchantNo())
                                 .eq(OrderTradeVo::getOrderNo, orderTradeDto.getOrderNo())
                 );
                 if (exists != null) {
                     throw  new BusinessException("订单已经存在");
                 }
                 CompletableFuture<MerchantVo> merInfo = getMerInfo(orderTradeDto.getMerchantNo());
                 MerchantVo merchantVo = merInfo.get();
                 log.info("当前商户的商户信息:{]",merchantVo);
                 if (Objects.isNull(merchantVo) || !MerchantEnum.NOMAL.getCode().equals(merchantVo.getMerchantType())) {
                     throw new BusinessException("商户状态不正确");
                 }
                 OrderTradeVo andPay = orderTradeService.createAndPay(orderTradeDto);
                 return andPay;
             }else{
                 throw new BusinessException("订单号重复错误" );
             }
        }catch (Exception e){
            log.error("order create fail message:{}",e.getMessage() );
            throw e;
        }

    }
    public  CompletableFuture<MerchantVo> getMerInfo(String merchantNo) throws Exception {
        CompletableFuture<MerchantVo> merInfo = CompletableFuture.supplyAsync(() -> {
            try {
                Result<MerchantVo> qmerchantInfo = merchantFeginClient.query(merchantNo);
                if (Objects.nonNull(qmerchantInfo) && ResultEnum.SUCESS.getCode() != qmerchantInfo.getCode()) {
                    throw new BusinessException(qmerchantInfo.getMsg());
                }
                return qmerchantInfo.getData();
            } catch (Exception e) {
                throw new BusinessException("商户系统失败");

            }
        }, threadPoolExecutor).handle((result,ex)->{
            if (ex != null) {
                Throwable cause = ex.getCause();
                log.error("查询商户异常", cause);
                if (cause instanceof BusinessException) {
                    throw (BusinessException) cause;
                }
                throw new BusinessException("商户系统调用失败");
            }
            return result;
        });
        return merInfo;
    }

    /**
     * 查询商户信息
     * @param req
     * @return
     * @throws Exception
     */
    public CompletableFuture<OrderTradeVo> queryCost(OrderTradeDto req) throws Exception {
        CompletableFuture<OrderTradeVo>  complatable = CompletableFuture.supplyAsync(() -> {
            OrderTradeVo exists = orderTradeMapper.selectOne(
                    Wrappers.<OrderTradeVo>lambdaQuery()
                            .eq(OrderTradeVo::getMerchantNo, req.getMerchantNo())
                            .eq(OrderTradeVo::getOrderNo, req.getOrderNo())
            );
            return exists;
        }, threadPoolExecutor);
        return complatable;
    };

//    /**
//     * 查询商户费率配置
//     * @param req
//     * @return
//     */
//    public CompletableFuture<BigDecimal> caculateFee(OrderTradeDto req){
//        CompletableFuture.supplyAsync(()->{
//            MerchantFeeDto merchantFeeDto = new MerchantFeeDto();
//            merchantFeeDto.setMerchantNo(req.getMerchantNo());
//            merchantFeeDto.setBizType(req.getBizType());
//            merchantFeginClient.queryFee()
//            return null;
//        },threadPoolExecutor).thenApply(result->{
//            return null;
//        });
//        return null;
//    }

}
