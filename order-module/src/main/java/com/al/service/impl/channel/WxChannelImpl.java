package com.al.service.impl.channel;

import com.al.bean.business.TradeChannelEnum;
import com.al.service.channel.TradeChannel;
import com.al.common.util.RestTemplateUtil;
import com.al.common.util.RsaUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component("WX_PAY")
@Slf4j
public class WxChannelImpl implements TradeChannel {
    @Autowired
    @Qualifier("customRestTemplate")
    private RestTemplate restTemplate;
    @Override
    public TradeChannelEnum supported() {
        return TradeChannelEnum.WX_PAY ;
    }

    @Override
    public Object pay(Map<String,Object> trade) throws Exception {
        try {
            // 1. 组装请求参数
            Map<String, Object> request = new HashMap<>();
            request.put("mch_id", "1900000109");
            request.put("out_trade_no", trade.get("out_trade_no"));
            request.put("total_fee", ((BigDecimal) trade.get("payAmount")).multiply(BigDecimal.valueOf(100)).intValue());
            request.put("currency", trade.get("currency"));
            request.put("notify_url", "https://xxx.com/pay/callback/wechat");

            // 2. 签名（示例）
            String privateKey = null;
            String content = RsaUtil.buildSignContent(request);
            String sign = RsaUtil.sign(content, privateKey);
            request.put("sign", sign);
            log.info("request param sign complate:{}", sign);
            // 3. 发送HTTP请求
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity =
                    new HttpEntity<>(request, headers);

            String url = "https://api.wechatpay.com/pay/unifiedorder";
            String response =
                    RestTemplateUtil.postJson(restTemplate, url, entity, String.class);

            log.info("wechat pay response: {}", response);

            // 4. 解析响应（示例）
            if (Integer.valueOf(response) == HttpStatus.OK.value()) {
                // 假设成功
                return "success";
            }

            return "fail";
        }catch (Exception e){
            log.error("wx channel pay error:{}", e.getMessage());
            throw e;
        }
    }
    public String sign(Map<String,Object> params,String key){
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (entry.getValue() != null && !"".equals(entry.getValue())) {
                sb.append(entry.getKey())
                        .append("=")
                        .append(entry.getValue())
                        .append("&");
            }
        }
        sb.append("key=").append(key);
        return sb.toString();
    }
}
