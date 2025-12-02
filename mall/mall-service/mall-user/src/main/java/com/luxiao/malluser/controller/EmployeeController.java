package com.luxiao.malluser.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luxiao.mallcommon.api.ApiResponse;
import com.luxiao.mallmodel.user.Employee;
import com.luxiao.mallsecurity.crypto.RsaCrypto;
import com.luxiao.malluser.dto.EmployeeLoginReq;
import com.luxiao.malluser.dto.EmployeeLoginResp;
import com.luxiao.malluser.service.EmployeeService;
import com.luxiao.malluser.dto.EmployeeRegisterReq;
import com.luxiao.malluser.dto.EmployeeUpdateReq;
import com.luxiao.malluser.vo.EmployeeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employee")
@Tag(name = "Employee APIs")
public class EmployeeController {

    private final EmployeeService employeeService;


    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }


    @PostMapping("/login")
    @Operation(summary = "员工登录")
    public ResponseEntity<ApiResponse<EmployeeLoginResp>> login(@Valid @RequestBody EmployeeLoginReq req) {
        return ResponseEntity.ok(ApiResponse.ok(employeeService.login(req)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询员工信息")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<Employee>> get(@PathVariable Long id) {
        Employee e = employeeService.getById(id);
        if (e != null) { e.setPassword(null); }
        return ResponseEntity.ok(ApiResponse.ok(e));
    }

    @GetMapping
    @Operation(summary = "分页查询员工")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<Page<EmployeeVO>>> page(@RequestParam(defaultValue = "1") int page,
                                                           @RequestParam(defaultValue = "10") int size,
                                                           @RequestParam(required = false) String username) {
        return ResponseEntity.ok(ApiResponse.ok(employeeService.pageEmployees(page, size, username)));
    }

    @PostMapping("/register")
    @Operation(summary = "员工注册")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<Employee>> register(@Valid @RequestBody EmployeeRegisterReq req) {
        return ResponseEntity.ok(ApiResponse.ok(employeeService.register(req)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新员工信息")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<Employee>> update(@PathVariable Long id, @Valid @RequestBody EmployeeUpdateReq req) {
        return ResponseEntity.ok(ApiResponse.ok(employeeService.updateProfile(id, req)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除员工")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<Boolean>> delete(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(employeeService.removeEmployee(id)));
    }

    @PostMapping("/{id}/reset-password")
    @Operation(summary = "重置员工密码为123456")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@PathVariable Long id) {
        employeeService.resetPassword(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
