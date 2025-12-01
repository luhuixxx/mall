package com.luxiao.malluser.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luxiao.mallcommon.api.ApiResponse;
import com.luxiao.mallmodel.user.Permission;
import com.luxiao.mallmodel.user.Role;
import com.luxiao.malluser.dto.AssignRolesReq;
import com.luxiao.malluser.dto.CreateRoleReq;
import com.luxiao.malluser.dto.SetRolePermissionsReq;
import com.luxiao.malluser.dto.UpdateRoleReq;
import com.luxiao.malluser.service.RoleRbacService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/role")
@Tag(name = "Role RBAC APIs")
public class RoleController {

    private final RoleRbacService roleService;

    public RoleController(RoleRbacService roleService) {
        this.roleService = roleService;
    }

    @PostMapping
    @Operation(summary = "创建角色")
    @PreAuthorize("hasAuthority('role:write')")
    public ResponseEntity<ApiResponse<Role>> create(@Valid @RequestBody CreateRoleReq req) {
        Role r = roleService.create(req.getName(), req.getDescription());
        return ResponseEntity.ok(ApiResponse.ok(r));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新角色")
    @PreAuthorize("hasAuthority('role:write')")
    public ResponseEntity<ApiResponse<Role>> update(@PathVariable Long id, @Valid @RequestBody UpdateRoleReq req) {
        Role r = roleService.update(id, req.getName(), req.getDescription());
        return ResponseEntity.ok(ApiResponse.ok(r));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除角色")
    @PreAuthorize("hasAuthority('role:write')")
    public ResponseEntity<ApiResponse<Boolean>> delete(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(roleService.delete(id)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询角色")
    @PreAuthorize("hasAuthority('role:read')")
    public ResponseEntity<ApiResponse<Role>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(roleService.get(id)));
    }

    @GetMapping
    @Operation(summary = "分页查询角色")
    @PreAuthorize("hasAuthority('role:read')")
    public ResponseEntity<ApiResponse<Page<Role>>> page(@RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "10") int size,
                                                        @RequestParam(required = false) String name) {
        return ResponseEntity.ok(ApiResponse.ok(roleService.page(page, size, name)));
    }

    @PostMapping("/{id}/permissions")
    @Operation(summary = "设置角色权限")
    @PreAuthorize("hasAuthority('role:write')")
    public ResponseEntity<ApiResponse<Void>> setRolePermissions(@PathVariable Long id, @Valid @RequestBody SetRolePermissionsReq req) {
        roleService.setRolePermissions(id, req.getPermissionIds());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/{id}/permissions")
    @Operation(summary = "查询角色权限")
    @PreAuthorize("hasAuthority('role:read')")
    public ResponseEntity<ApiResponse<List<Permission>>> listRolePermissions(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(roleService.listRolePermissions(id)));
    }

    @PostMapping("/assign")
    @Operation(summary = "为员工分配角色")
    @PreAuthorize("hasAuthority('role:write')")
    public ResponseEntity<ApiResponse<Void>> assignRoles(@Valid @RequestBody AssignRolesReq req) {
        roleService.assignEmployeeRoles(req.getEmployeeId(), req.getRoleIds());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/employee/{employeeId}/permissions")
    @Operation(summary = "查询员工权限")
    @PreAuthorize("hasAuthority('role:read')")
    public ResponseEntity<ApiResponse<List<Permission>>> listEmployeePermissions(@PathVariable Long employeeId) {
        return ResponseEntity.ok(ApiResponse.ok(roleService.listEmployeePermissions(employeeId)));
    }

    @GetMapping("/permissions")
    @Operation(summary = "查询所有权限")
    @PreAuthorize("hasAuthority('role:read')")
    public ResponseEntity<ApiResponse<List<Permission>>> listAllPermissions() {
        return ResponseEntity.ok(ApiResponse.ok(roleService.listAllPermissions()));
    }
}
