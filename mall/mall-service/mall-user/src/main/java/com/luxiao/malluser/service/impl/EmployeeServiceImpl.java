package com.luxiao.malluser.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luxiao.mallmodel.user.Employee;
import com.luxiao.mallmodel.user.EmployeeRole;
import com.luxiao.mallmodel.user.Role;
import com.luxiao.mallmodel.user.RolePermission;
import com.luxiao.mallmodel.user.Permission;
import com.luxiao.malluser.dto.EmployeeLoginReq;
import com.luxiao.malluser.dto.EmployeeLoginResp;
import com.luxiao.malluser.dto.EmployeeRegisterReq;
import com.luxiao.malluser.dto.EmployeeUpdateReq;
import com.luxiao.malluser.mapper.EmployeeMapper;
import com.luxiao.malluser.mapper.EmployeeRoleMapper;
import com.luxiao.malluser.mapper.RoleMapper;
import com.luxiao.malluser.mapper.RolePermissionMapper;
import com.luxiao.malluser.mapper.PermissionMapper;
import com.luxiao.malluser.service.EmployeeService;
import com.alibaba.fastjson2.JSON;
import com.luxiao.mallsecurity.crypto.RsaCrypto;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {

    private final EmployeeRoleMapper employeeRoleMapper;
    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionMapper permissionMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final RsaCrypto rsaCrypto;

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    @Value("${security.jwt.ttl}")
    private long jwtTtlSeconds;

    public EmployeeServiceImpl(EmployeeRoleMapper employeeRoleMapper,
                               RoleMapper roleMapper,
                               RolePermissionMapper rolePermissionMapper,
                               PermissionMapper permissionMapper,
                               StringRedisTemplate stringRedisTemplate,
                               RsaCrypto rsaCrypto) {
        this.employeeRoleMapper = employeeRoleMapper;
        this.roleMapper = roleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.permissionMapper = permissionMapper;
        this.stringRedisTemplate = stringRedisTemplate;
        this.rsaCrypto = rsaCrypto;
    }

    @Override
    public EmployeeLoginResp login(EmployeeLoginReq req) {
        LambdaQueryWrapper<Employee> qw = new LambdaQueryWrapper<>();
        qw.eq(Employee::getUsername, req.getUsername());
        Employee emp = this.getOne(qw);
        if (emp == null) {
            throw new IllegalArgumentException("员工不存在");
        }
        String raw = rsaCrypto.decrypt(req.getPassword());
        String encoded = sha256(raw);
        if (!encoded.equals(emp.getPassword())) {
            throw new IllegalArgumentException("密码不正确");
        }
        emp.setPassword(null);
        String token = generateToken(emp);
        List<String> roleNames = getRoleNames(emp.getId());
        List<String> perms = getPermissions(emp.getId());
        EmployeeLoginResp resp = new EmployeeLoginResp(token, emp, roleNames, perms);
        cacheEmployeeLogin(emp, resp);
        return resp;
    }

    @Override
    public Page<Employee> pageEmployees(int page, int size, String username) {
        LambdaQueryWrapper<Employee> qw = new LambdaQueryWrapper<>();
        qw.like(username != null && !username.isBlank(), Employee::getUsername, username);
        Page<Employee> p = this.page(Page.of(page, size), qw);
        p.getRecords().forEach(e -> e.setPassword(null));
        return p;
    }

    @Override
    public Employee register(EmployeeRegisterReq req) {
        LambdaQueryWrapper<Employee> qw = new LambdaQueryWrapper<>();
        qw.eq(Employee::getUsername, req.getUsername());
        if (this.getOne(qw) != null) {
            throw new IllegalArgumentException("用户名已存在");
        }
        Employee e = new Employee();
        e.setUsername(req.getUsername());
        String raw = rsaCrypto.decrypt(req.getPassword());
        e.setPassword(sha256(raw));
        this.save(e);
        e.setPassword(null);
        return e;
    }

    @Override
    public Employee updateProfile(Long id, EmployeeUpdateReq req) {
        Employee e = this.getById(id);
        if (e == null) {
            throw new IllegalArgumentException("员工不存在");
        }
        e.setUsername(req.getUsername());
        this.updateById(e);
        e.setPassword(null);
        return e;
    }

    private String generateToken(Employee e) {
        byte[] key = Base64.getDecoder().decode(jwtSecret);
        System.out.println("key: " + key.length);
        long now = System.currentTimeMillis();
        java.util.Date issued = new java.util.Date(now);
        java.util.Date exp = new java.util.Date(now + jwtTtlSeconds * 1000);
        return Jwts.builder()
                .setSubject("emp:" + e.getId())
                .claim("username", e.getUsername())
                .claim("identity", "EMPLOYEE")
                .claim("userId", String.valueOf(e.getId()))
                .setIssuedAt(issued)
                .setExpiration(exp)
                .signWith(Keys.hmacShaKeyFor(key))
                .compact();
    }

    private List<String> getRoleNames(Long employeeId) {
        List<EmployeeRole> ers = employeeRoleMapper.selectList(new LambdaQueryWrapper<EmployeeRole>().eq(EmployeeRole::getEmployeeId, employeeId));
        Set<Long> roleIds = ers.stream().map(EmployeeRole::getRoleId).collect(Collectors.toSet());
        if (roleIds.isEmpty()) return java.util.List.of();
        return roleMapper.selectBatchIds(roleIds).stream().map(Role::getName).collect(Collectors.toList());
    }

    private List<String> getPermissions(Long employeeId) {
        List<EmployeeRole> ers = employeeRoleMapper.selectList(new LambdaQueryWrapper<EmployeeRole>().eq(EmployeeRole::getEmployeeId, employeeId));
        Set<Long> roleIds = ers.stream().map(EmployeeRole::getRoleId).collect(Collectors.toSet());
        if (roleIds.isEmpty()) return java.util.List.of();
        List<RolePermission> rps = rolePermissionMapper.selectList(new LambdaQueryWrapper<RolePermission>().in(RolePermission::getRoleId, roleIds));
        Set<Long> permIds = rps.stream().map(RolePermission::getPermissionId).collect(Collectors.toSet());
        if (permIds.isEmpty()) return java.util.List.of();
        return permissionMapper.selectBatchIds(permIds).stream().map(Permission::getPerms).collect(Collectors.toList());
    }

    private void cacheEmployeeLogin(Employee emp, EmployeeLoginResp resp) {
        try {
            String keyEmp = "auth:employee:" + emp.getId();
            String keyRoles = "auth:employee:" + emp.getId() + ":roles";
            String keyPerms = "auth:employee:" + emp.getId() + ":perms";
            String keyToken = "auth:token:" + resp.getToken();
            stringRedisTemplate.opsForValue().set(keyEmp, JSON.toJSONString(resp), java.time.Duration.ofSeconds(jwtTtlSeconds));
            stringRedisTemplate.opsForValue().set(keyRoles, JSON.toJSONString(resp.getRoles()), java.time.Duration.ofSeconds(jwtTtlSeconds));
            stringRedisTemplate.opsForValue().set(keyPerms, JSON.toJSONString(resp.getPermissions()), java.time.Duration.ofSeconds(jwtTtlSeconds));
            stringRedisTemplate.opsForValue().set(keyToken, "emp:" + emp.getId(), java.time.Duration.ofSeconds(jwtTtlSeconds));
        } catch (Exception e) {
            throw new RuntimeException("员工登录信息缓存失败", e);
        }
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
        } catch (NoSuchAlgorithmException e1) {
            throw new RuntimeException(e1);
        }
    }
}
