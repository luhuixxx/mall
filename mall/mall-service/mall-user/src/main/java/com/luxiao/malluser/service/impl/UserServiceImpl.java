package com.luxiao.malluser.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luxiao.mallmodel.user.User;
import com.luxiao.malluser.dto.UserLoginReq;
import com.luxiao.malluser.dto.LoginResp;
import com.alibaba.fastjson2.JSON;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import com.luxiao.malluser.dto.UserRegisterReq;
import com.luxiao.malluser.dto.UserUpdateReq;
import com.luxiao.malluser.dto.UserChangePasswordReq;
import com.luxiao.malluser.dto.UserUpdateBalanceReq;
import com.luxiao.mallmodel.user.dto.UserAdjustBalanceReq;
import com.luxiao.malluser.mapper.UserMapper;
import com.luxiao.malluser.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.luxiao.mallsecurity.crypto.RsaCrypto;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final StringRedisTemplate stringRedisTemplate;
    private final RsaCrypto rsaCrypto;

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    @Value("${security.jwt.ttl}")
    private long jwtTtlSeconds;

    public UserServiceImpl(StringRedisTemplate stringRedisTemplate, RsaCrypto rsaCrypto) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.rsaCrypto = rsaCrypto;
    }

    @Override
    @Transactional
    public User register(UserRegisterReq req) {
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
        qw.eq(User::getUsername, req.getUsername());
        if (this.getOne(qw) != null) {
            throw new IllegalArgumentException("用户名已存在");
        }
        User u = new User();
        u.setName(req.getName());
        u.setUsername(req.getUsername());
        String raw = rsaCrypto.decrypt(req.getPassword());
        u.setPassword(sha256(raw));
        u.setEmail(req.getEmail());
        u.setBalance(BigDecimal.ZERO);
        this.save(u);
        u.setPassword(null);
        return u;
    }

    @Override
    public String login(UserLoginReq req) {
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
        qw.eq(User::getUsername, req.getUsername());
        User u = this.getOne(qw);
        if (u == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        String raw = rsaCrypto.decrypt(req.getPassword());
        String encoded = sha256(raw);
        if (!encoded.equals(u.getPassword())) {
            throw new IllegalArgumentException("密码不正确");
        }
        u.setPassword(null);
        String token = generateToken(u);
        cacheLogin(u, token);
        return token;
    }

    @Override
    @Transactional
    public User updateProfile(Long id, UserUpdateReq req) {
        User u = this.getById(id);
        if (u == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        u.setName(req.getName());
        u.setEmail(req.getEmail());
        this.updateById(u);
        u.setPassword(null);
        return u;
    }

    @Override
    public Page<User> pageUsers(int page, int size, String username, String email) {
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
        qw.like(username != null && !username.isBlank(), User::getUsername, username)
          .like(email != null && !email.isBlank(), User::getEmail, email);
        Page<User> p = this.page(Page.of(page, size), qw);
        p.getRecords().forEach(u -> u.setPassword(null));
        return p;
    }

    @Override
    @Transactional
    public void changePassword(Long id, UserChangePasswordReq req) {
        User u = this.getById(id);
        if (u == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        String oldRaw = rsaCrypto.decrypt(req.getOldPassword());
        String newRaw = rsaCrypto.decrypt(req.getNewPassword());
        String oldEncoded = sha256(oldRaw);
        if (!oldEncoded.equals(u.getPassword())) {
            throw new IllegalArgumentException("原密码不正确");
        }
        u.setPassword(sha256(newRaw));
        this.updateById(u);
    }

    @Override
    public void resetPassword(Long id) {
        User u = this.getById(id);
        if (u == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        u.setPassword(sha256("123456"));
        this.updateById(u);
    }

    @Override
    @Transactional
    public User updateBalance(Long id, UserUpdateBalanceReq req) {
        User u = this.getById(id);
        if (u == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        u.setBalance(req.getBalance());
        this.updateById(u);
        u.setPassword(null);
        return u;
    }

    @Override
    @Transactional
    public User adjustBalance(Long id, UserAdjustBalanceReq req) {
        User u = this.getById(id);
        if (u == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        java.math.BigDecimal current = u.getBalance() == null ? java.math.BigDecimal.ZERO : u.getBalance();
        java.math.BigDecimal updated = current.add(req.getDelta());
        if (updated.compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("余额不足");
        }
        u.setBalance(updated);
        this.updateById(u);
        u.setPassword(null);
        return u;
    }

    @Override
    public long countAllUsers() {
        return this.count();
    }

    @Override
    public long countNewUsersLast7Days() {
        var now = java.time.LocalDateTime.now();
        var from = now.minusDays(7);
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User> qw = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        qw.ge(User::getCreatedTime, from);
        return this.count(qw);
    }

    private String sha256(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private String generateToken(User u) {
        byte[] key = java.util.Base64.getDecoder().decode(jwtSecret);
        long now = System.currentTimeMillis();
        java.util.Date issued = new java.util.Date(now);
        java.util.Date exp = new java.util.Date(now + jwtTtlSeconds * 1000);
        return Jwts.builder()
                .setSubject(String.valueOf(u.getId()))
                .claim("username", u.getUsername())
                .claim("identity", "USER")
                .claim("userId", String.valueOf(u.getId()))
                .setIssuedAt(issued)
                .setExpiration(exp)
                .signWith(Keys.hmacShaKeyFor(key))
                .compact();
    }

    private void cacheLogin(User u, String token) {
        try {
            String keyUser = "auth:user:" + u.getId();
            String keyToken = "auth:token:" + token;
            String json = JSON.toJSONString(new LoginResp(token, u));
            stringRedisTemplate.opsForValue().set(keyUser, json, java.time.Duration.ofSeconds(jwtTtlSeconds));
            stringRedisTemplate.opsForValue().set(keyToken, String.valueOf(u.getId()), java.time.Duration.ofSeconds(jwtTtlSeconds));
        } catch (Exception e) {
            throw new RuntimeException("登录信息缓存失败", e);
        }
    }
}
