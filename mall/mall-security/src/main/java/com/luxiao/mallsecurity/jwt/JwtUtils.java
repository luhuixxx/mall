package com.luxiao.mallsecurity.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtils {

    @Value("${security.jwt.secret}")
    private String secret;

    @Value("${security.jwt.ttl}")
    private long ttlSeconds;

    public String generate(Long userId, String username) {
        byte[] key = Base64.getDecoder().decode(secret);
        long now = System.currentTimeMillis();
        Date issued = new Date(now);
        Date exp = new Date(now + ttlSeconds * 1000);
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("username", username)
                .setIssuedAt(issued)
                .setExpiration(exp)
                .signWith(Keys.hmacShaKeyFor(key))
                .compact();
    }

    public Claims parse(String token) {
        byte[] key = Base64.getDecoder().decode(secret);
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(key))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}

