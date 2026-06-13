package com.al.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component

public class AntiBotFilter implements GlobalFilter {
    Logger log = LoggerFactory.getLogger(AntiBotFilter.class);
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("Black and white list filter start ");
        String ip = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        return chain.filter(exchange);
    }
}
