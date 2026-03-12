package com.al.config;

import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPathPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.GatewayApiDefinitionManager;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.SentinelGatewayFilter;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.exception.SentinelGatewayBlockExceptionHandler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * Sentinel网关限流配置
 * 为商户、订单、账户模块配置限流规则
 */
@Configuration
public class SentinelGatewayConfig {

    private final List<ViewResolver> viewResolvers;
    private final ServerCodecConfigurer serverCodecConfigurer;

    public SentinelGatewayConfig(ObjectProvider<List<ViewResolver>> viewResolversProvider,
                                 ServerCodecConfigurer serverCodecConfigurer) {
        this.viewResolvers = viewResolversProvider.getIfAvailable(Collections::emptyList);
        this.serverCodecConfigurer = serverCodecConfigurer;
    }

    /**
     * 配置Sentinel网关过滤器
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public GlobalFilter sentinelGatewayFilter() {
        return new SentinelGatewayFilter();
    }

    /**
     * 配置Sentinel网关异常处理器
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SentinelGatewayBlockExceptionHandler sentinelGatewayBlockExceptionHandler() {
        return new SentinelGatewayBlockExceptionHandler(viewResolvers, serverCodecConfigurer);
    }

    /**
     * 自定义限流返回信息
     */
    @PostConstruct
    public void initBlockHandlers() {
        BlockRequestHandler blockRequestHandler = new BlockRequestHandler() {
            @Override
            public Mono<ServerResponse> handleRequest(ServerWebExchange exchange, Throwable t) {
                Map<String, Object> map = new HashMap<>();
                map.put("code", 429);
                map.put("message", "请求过于频繁，请稍后再试");
                map.put("route", exchange.getRequest().getPath().value());
                map.put("timestamp", System.currentTimeMillis());

                return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(map));
            }
        };
        GatewayCallbackManager.setBlockHandler(blockRequestHandler);
    }

    /**
     * 初始化限流规则
     */
    @PostConstruct
    public void initGatewayRules() {
        Set<GatewayFlowRule> rules = new HashSet<>();

        // 商户模块限流规则：每秒10个请求
        rules.add(new GatewayFlowRule("merchant-route")
                .setCount(10)
                .setIntervalSec(1)
                .setBurst(5)
                .setControlBehavior(0) // 0:直接拒绝, 1:Warm Up, 2:匀速排队
        );

        // 订单模块限流规则：每秒20个请求
        rules.add(new GatewayFlowRule("order-route")
                .setCount(20)
                .setIntervalSec(1)
                .setBurst(10)
                .setControlBehavior(0)
        );

        // 账户模块限流规则：每秒15个请求
        rules.add(new GatewayFlowRule("account-route")
                .setCount(15)
                .setIntervalSec(1)
                .setBurst(8)
                .setControlBehavior(0)
        );

        // API分组配置（可选）
        initCustomizedApis();

        GatewayRuleManager.loadRules(rules);
    }

    /**
     * 自定义API分组（可选）
     */
    private void initCustomizedApis() {
        Set<ApiDefinition> definitions = new HashSet<>();

        // 商户API分组
        ApiDefinition merchantApi = new ApiDefinition("merchant-api")
                .setPredicateItems(new HashSet<ApiPredicateItem>() {{
                    add(new ApiPathPredicateItem().setPattern("/merchant/**"));
                }});

        // 订单API分组
        ApiDefinition orderApi = new ApiDefinition("order-api")
                .setPredicateItems(new HashSet<ApiPredicateItem>() {{
                    add(new ApiPathPredicateItem().setPattern("/order/**"));
                }});

        // 账户API分组
        ApiDefinition accountApi = new ApiDefinition("account-api")
                .setPredicateItems(new HashSet<ApiPredicateItem>() {{
                    add(new ApiPathPredicateItem().setPattern("/account/**"));
                }});

        definitions.add(merchantApi);
        definitions.add(orderApi);
        definitions.add(accountApi);

        GatewayApiDefinitionManager.loadApiDefinitions(definitions);
    }
}