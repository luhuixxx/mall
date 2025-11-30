package com.luxiao.malluser.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luxiao.mallmodel.user.User;
import com.luxiao.mallcommon.api.ApiResponse;
import com.luxiao.malluser.dto.UserLoginReq;
import com.luxiao.malluser.dto.LoginResp;
import com.luxiao.malluser.dto.UserRegisterReq;
import com.luxiao.malluser.dto.UserUpdateReq;
import com.luxiao.malluser.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@Tag(name = "User APIs")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public ResponseEntity<ApiResponse<User>> register(@Valid @RequestBody UserRegisterReq req) {
        return ResponseEntity.ok(ApiResponse.ok(userService.register(req)));
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public ResponseEntity<ApiResponse<String>> login(@Valid @RequestBody UserLoginReq req) {
        return ResponseEntity.ok(ApiResponse.ok(userService.login(req)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询用户信息")
    @PreAuthorize("hasAnyRole('USER','EMPLOYEE')")
    public ResponseEntity<ApiResponse<User>> get(@PathVariable Long id) {
        User u = userService.getById(id);
        if (u != null) {
            u.setPassword(null);
        }
        return ResponseEntity.ok(ApiResponse.ok(u));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新用户信息")
    @PreAuthorize("hasAnyRole('USER','EMPLOYEE')")
    public ResponseEntity<ApiResponse<User>> update(@PathVariable Long id, @Valid @RequestBody UserUpdateReq req) {
        return ResponseEntity.ok(ApiResponse.ok(userService.updateProfile(id, req)));
    }

    @GetMapping
    @Operation(summary = "分页查询用户")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<Page<User>>> page(@RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int size,
                                           @RequestParam(required = false) String username,
                                           @RequestParam(required = false) String email) {
        return ResponseEntity.ok(ApiResponse.ok(userService.pageUsers(page, size, username, email)));
    }
}

