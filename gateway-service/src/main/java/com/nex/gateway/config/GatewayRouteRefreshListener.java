package com.nex.gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Nacos 配置中心路由变化刷新路由缓存
 */
@Component
public class GatewayRouteRefreshListener {

    private static final Logger log = LoggerFactory.getLogger(GatewayRouteRefreshListener.class);

    private final ApplicationEventPublisher applicationEventPublisher;

    public GatewayRouteRefreshListener(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @EventListener
    @Order(Ordered.LOWEST_PRECEDENCE)
    public void onEnvironmentChange(EnvironmentChangeEvent event) {

        log.info("检测到配置环境变化，准备刷新 Gateway 路由");

        applicationEventPublisher.publishEvent(new RefreshRoutesEvent(this));
    }
}
