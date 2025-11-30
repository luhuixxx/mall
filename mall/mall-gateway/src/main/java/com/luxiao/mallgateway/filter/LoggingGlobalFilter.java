package com.luxiao.mallgateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.client.ServiceInstance;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class LoggingGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(LoggingGlobalFilter.class);

    @Value("${gateway.logging.enabled:true}")
    private boolean enabled;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!enabled) {
            return chain.filter(exchange);
        }
        long start = System.currentTimeMillis();
        ServerHttpRequest req = exchange.getRequest();
        InetSocketAddress remote = req.getRemoteAddress();
        Map<String, String> headers = maskedHeaders(req.getHeaders());
        log.info("REQ method={} uri={} remote={} headers={}", req.getMethod(), req.getURI(), remote, headers);
        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        URI requestUrl = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);
        List<URI> originalUrls = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ORIGINAL_REQUEST_URL_ATTR);
        String schemePrefix = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_SCHEME_PREFIX_ATTR);
        if (route != null) {
            log.info("ROUTE id={} targetUri={} requestUrl={} originalUrl={} schemePrefix={}",
                    route.getId(), route.getUri(), requestUrl, originalUrls, schemePrefix);
        }
        Response<ServiceInstance> lb = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_LOADBALANCER_RESPONSE_ATTR);
        if (lb != null && lb.getServer() != null) {
            ServiceInstance si = lb.getServer();
            log.info("LB serviceId={} host={} port={} instanceId={} metadata={}",
                    si.getServiceId(), si.getHost(), si.getPort(), si.getInstanceId(), si.getMetadata());
        }
        return chain.filter(exchange).doFinally(s -> {
            ServerHttpResponse resp = exchange.getResponse();
            String len = resp.getHeaders().getFirst(HttpHeaders.CONTENT_LENGTH);
            long cost = System.currentTimeMillis() - start;
            log.info("RESP status={} duration={}ms content-length={}", resp.getStatusCode(), cost, len);
            URI finalUrl = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);
            if (finalUrl != null) {
                log.info("FORWARD finalUrl={}", finalUrl);
            }
        });
    }

    private Map<String, String> maskedHeaders(HttpHeaders httpHeaders) {
        Map<String, String> m = new LinkedHashMap<>();
        httpHeaders.forEach((k, v) -> {
            if (HttpHeaders.AUTHORIZATION.equalsIgnoreCase(k) && !v.isEmpty()) {
                String val = v.get(0);
                if (val.startsWith("Bearer ") && val.length() > 20) {
                    m.put(k, val.substring(0, 16) + "..." + val.substring(val.length() - 4));
                } else {
                    m.put(k, "***");
                }
            } else {
                m.put(k, String.join(",", v));
            }
        });
        return m;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
