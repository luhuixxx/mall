package com.luxiao.malluser.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.luxiao.mallmodel.user.Permission;
import com.luxiao.mallmodel.user.Role;

import java.util.List;

public interface RoleRbacService extends IService<Role> {
    Role create(String name, String description);
    Role update(Long id, String name, String description);
    boolean delete(Long id);
    Role get(Long id);
    Page<Role> page(int page, int size, String name);
    void setRolePermissions(Long roleId, List<Long> permissionIds);
    List<Permission> listRolePermissions(Long roleId);
    void assignEmployeeRoles(Long employeeId, List<Long> roleIds);
    List<Permission> listEmployeePermissions(Long employeeId);
    List<Permission> listAllPermissions();
}
