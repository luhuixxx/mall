package com.luxiao.mallsecurity.config;

import com.luxiao.mallsecurity.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import com.luxiao.mallcommon.security.WhiteList;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    private String[] whiteList = WhiteList.PATHS;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/product/**").hasAnyAuthority("product:write", "ROLE_INTERNAL_SERVICE")
                        .requestMatchers(HttpMethod.PUT, "/api/product/**").hasAnyAuthority("product:write", "ROLE_INTERNAL_SERVICE")
                        .requestMatchers(HttpMethod.DELETE, "/api/product/**").hasAnyAuthority("product:write", "ROLE_INTERNAL_SERVICE")
                        .requestMatchers("/api/employee/**").hasRole("EMPLOYEE")
                        .requestMatchers("/api/order/**", "/api/shoppingcart/**").hasAnyRole("USER", "INTERNAL_SERVICE","EMPLOYEE")
                        .requestMatchers(whiteList).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/product/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers(
                "/doc.html",
                "/v3/api-docs/**",
                "/swagger-ui.html",
                "/swagger-ui/**",
                "/swagger-resources/**",
                "/webjars/**",
                "/static/**",
                "/public/**"
        )
                .requestMatchers(whiteList);
    }


}
