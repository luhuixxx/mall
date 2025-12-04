package com.luxiao.mallsecurity.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import com.luxiao.mallcommon.security.WhiteList;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private StringRedisTemplate stringRedisTemplate;
    @Value("${security.internal.secret:}")
    private String internalSecret;
    @Value("${security.internal.clockSkewSeconds:120}")
    private long clockSkewSeconds;

    public JwtAuthenticationFilter(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (WhiteList.matchesPrefix(path)) {
            filterChain.doFilter(request, response);
            return;
        }
        String identity = request.getHeader("X-Auth-Identity");
        String userId = request.getHeader("X-Auth-UserId");
        if (identity != null && userId != null) {
            try {
                List<GrantedAuthority> authorities = buildAuthorities(identity, userId);
                var authentication = new UsernamePasswordAuthenticationToken(userId, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
                response.setStatus(401);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"message\":\"unauthorized\"}");
                return;
            }
        } else if (isInternalRequestAuthenticated(request)) {
            String serviceName = request.getHeader("X-Internal-Service");
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_INTERNAL_SERVICE"));
            var authentication = new UsernamePasswordAuthenticationToken(serviceName, null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }

    private boolean isWhitelisted(String path) {
        return WhiteList.matchesPrefix(path);
    }

    private List<GrantedAuthority> buildAuthorities(String identity, String userId) throws JsonProcessingException {
        if ("EMPLOYEE".equalsIgnoreCase(identity)) {
            String rolesJson = stringRedisTemplate.opsForValue().get("auth:employee:" + userId + ":roles");
            String permsJson = stringRedisTemplate.opsForValue().get("auth:employee:" + userId + ":perms");
            if (rolesJson == null || permsJson == null) {
                throw new IllegalStateException("unauthenticated");
            }
            List<String> roles = new ObjectMapper().readValue(rolesJson, new TypeReference<List<String>>() {});
            List<String> perms = new ObjectMapper().readValue(permsJson, new TypeReference<List<String>>() {});
            List<GrantedAuthority> auths = roles.stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r.toUpperCase())).collect(Collectors.toList());
            auths.add(new SimpleGrantedAuthority("ROLE_EMPLOYEE"));
            auths.addAll(perms.stream().map(SimpleGrantedAuthority::new).toList());
            return auths;
        } else if ("USER".equalsIgnoreCase(identity)) {
            //String permsJson = stringRedisTemplate.opsForValue().get("auth:user:" + userId + ":perms");
            List<GrantedAuthority> auths = new ArrayList<>();
            auths.add(new SimpleGrantedAuthority("ROLE_USER"));
//            if (permsJson != null) {
//                List<String> perms = new ObjectMapper().readValue(permsJson, new TypeReference<List<String>>() {});
//                auths.addAll(perms.stream().map(SimpleGrantedAuthority::new).toList());
//            }
            return auths;
        } else {
            throw new IllegalStateException("unauthenticated");
        }
    }

    private boolean isInternalRequestAuthenticated(HttpServletRequest request) {
        String svc = request.getHeader("X-Internal-Service");
        String ts = request.getHeader("X-Internal-Timestamp");
        String sign = request.getHeader("X-Internal-Sign");
        if (svc == null || ts == null || sign == null) {
            return false;
        }
        if (internalSecret == null || internalSecret.isEmpty()) {
            return false;
        }
        try {
            long timestamp = Long.parseLong(ts);
            long now = Instant.now().toEpochMilli();
            long skewMs = clockSkewSeconds * 1000L;
            if (Math.abs(now - timestamp) > skewMs) {
                return false;
            }
            String payload = svc + ":" + ts;
            String expected = hmacSha256Hex(internalSecret, payload);
            return expected.equalsIgnoreCase(sign);
        } catch (Exception e) {
            return false;
        }
    }

    private String hmacSha256Hex(String secret, String data) throws GeneralSecurityException {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
