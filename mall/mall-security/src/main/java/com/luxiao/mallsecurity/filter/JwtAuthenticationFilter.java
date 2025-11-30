package com.luxiao.mallsecurity.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private StringRedisTemplate stringRedisTemplate;

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
            }
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
            List<String> roles = rolesJson != null ? new ObjectMapper().readValue(rolesJson, new TypeReference<List<String>>() {}) : List.of();
            List<String> perms = permsJson != null ? new ObjectMapper().readValue(permsJson, new TypeReference<List<String>>() {}) : List.of();
            List<GrantedAuthority> auths = roles.stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r.toUpperCase())).collect(Collectors.toList());
            auths.add(new SimpleGrantedAuthority("ROLE_EMPLOYEE"));
            auths.addAll(perms.stream().map(SimpleGrantedAuthority::new).toList());
            return auths;
        } else {
            List<GrantedAuthority> auths = new ArrayList<>();
            auths.add(new SimpleGrantedAuthority("ROLE_USER"));
            return auths;
        }
    }
}
