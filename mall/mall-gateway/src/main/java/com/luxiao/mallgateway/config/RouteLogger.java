package com.luxiao.mallgateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.stereotype.Component;

@Component
public class RouteLogger {

    private static final Logger log = LoggerFactory.getLogger(RouteLogger.class);
    private final RouteDefinitionLocator locator;

    public RouteLogger(RouteDefinitionLocator locator) {
        this.locator = locator;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        logRoutes("startup");
    }

    @EventListener(EnvironmentChangeEvent.class)
    public void onEnvChange(EnvironmentChangeEvent event) {
        logRoutes("env-change");
    }

    @EventListener(RefreshScopeRefreshedEvent.class)
    public void onRefresh(RefreshScopeRefreshedEvent event) {
        logRoutes("refresh");
    }

    private void logRoutes(String phase) {
        locator.getRouteDefinitions().collectList().subscribe(list -> {
            log.info("Gateway routes [{}] count={}", phase, list.size());
            list.forEach(def -> {
                String predicates = def.getPredicates() != null ? def.getPredicates().toString() : "[]";
                String filters = def.getFilters() != null ? def.getFilters().toString() : "[]";
                log.info("route id={}, uri={}, predicates={}, filters={}", def.getId(), def.getUri(), predicates, filters);
            });
        });
    }
}

