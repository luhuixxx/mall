package com.luxiao.mallgateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import com.luxiao.mallcommon.security.WhiteList;

@Component
public class JwtGlobalFilter implements GlobalFilter, Ordered {

    @Value("${security.jwt.secret}")
    private String secret;

    private static final List<String> WHITELIST = List.of(WhiteList.PATHS);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (WhiteList.matchesPrefix(path)) {
            return chain.filter(exchange);
        }

        String auth = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (auth == null || !auth.startsWith("Bearer ")) {
            return unauthorized(exchange, "缺少或无效的Authorization头");
        }
        String token = auth.substring(7);
        try {
            byte[] keyBytes = Base64.getDecoder().decode(secret);
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(keyBytes))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            if (claims.getExpiration() != null && claims.getExpiration().getTime() < System.currentTimeMillis()) {
                return unauthorized(exchange, "Token已过期");
            }
            String subject = claims.getSubject();
            String identity;
            String userId;
            if (subject != null && subject.startsWith("emp:")) {
                identity = "EMPLOYEE";
                userId = subject.substring(4);
            } else if (subject != null) {
                identity = "USER";
                userId = subject;
            } else {
                return unauthorized(exchange, "Token缺少主题");
            }
            var mutatedRequest = exchange.getRequest().mutate()
                    .header("X-Auth-Identity", identity)
                    .header("X-Auth-UserId", userId)
                    .build();
            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        } catch (Exception e) {
            return unauthorized(exchange, "Token解析失败");
        }
    }

    private boolean isWhitelisted(String path) {
        return WhiteList.matchesPrefix(path);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String msg) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"code\":401,\"message\":\"" + msg + "\"}";
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
