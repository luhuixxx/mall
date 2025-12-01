package com.luxiao.malluser.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luxiao.mallmodel.user.EmployeeRole;
import com.luxiao.mallmodel.user.Permission;
import com.luxiao.mallmodel.user.Role;
import com.luxiao.mallmodel.user.RolePermission;
import com.luxiao.malluser.mapper.EmployeeRoleMapper;
import com.luxiao.malluser.mapper.PermissionMapper;
import com.luxiao.malluser.mapper.RoleMapper;
import com.luxiao.malluser.mapper.RolePermissionMapper;
import com.luxiao.malluser.service.RoleRbacService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoleRbacServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleRbacService {
    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionMapper permissionMapper;
    private final EmployeeRoleMapper employeeRoleMapper;

    public RoleRbacServiceImpl(RolePermissionMapper rolePermissionMapper,
                               PermissionMapper permissionMapper,
                               EmployeeRoleMapper employeeRoleMapper) {
        this.rolePermissionMapper = rolePermissionMapper;
        this.permissionMapper = permissionMapper;
        this.employeeRoleMapper = employeeRoleMapper;
    }

    @Override
    @Transactional
    public Role create(String name, String description) {
        Role r = new Role();
        r.setName(name);
        r.setDescription(description);
        this.save(r);
        return r;
    }

    @Override
    @Transactional
    public Role update(Long id, String name, String description) {
        Role r = this.getById(id);
        if (r == null) throw new IllegalArgumentException("角色不存在");
        r.setName(name);
        r.setDescription(description);
        this.updateById(r);
        return r;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        rolePermissionMapper.delete(new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, id));
        employeeRoleMapper.delete(new LambdaQueryWrapper<EmployeeRole>().eq(EmployeeRole::getRoleId, id));
        return this.removeById(id);
    }

    @Override
    public Role get(Long id) {
        return this.getById(id);
    }

    @Override
    public Page<Role> page(int page, int size, String name) {
        LambdaQueryWrapper<Role> qw = new LambdaQueryWrapper<>();
        qw.like(name != null && !name.isBlank(), Role::getName, name);
        return this.page(Page.of(page, size), qw);
    }

    @Override
    @Transactional
    public void setRolePermissions(Long roleId, List<Long> permissionIds) {
        rolePermissionMapper.delete(new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, roleId));
        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (Long pid : permissionIds) {
                RolePermission rp = new RolePermission();
                rp.setRoleId(roleId);
                rp.setPermissionId(pid);
                rolePermissionMapper.insert(rp);
            }
        }
    }

    @Override
    public List<Permission> listRolePermissions(Long roleId) {
        List<RolePermission> rps = rolePermissionMapper.selectList(new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, roleId));
        Set<Long> permIds = rps.stream().map(RolePermission::getPermissionId).collect(Collectors.toSet());
        if (permIds.isEmpty()) return List.of();
        return permissionMapper.selectBatchIds(permIds);
    }

    @Override
    @Transactional
    public void assignEmployeeRoles(Long employeeId, List<Long> roleIds) {
        employeeRoleMapper.delete(new LambdaQueryWrapper<EmployeeRole>().eq(EmployeeRole::getEmployeeId, employeeId));
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long rid : roleIds) {
                EmployeeRole er = new EmployeeRole();
                er.setEmployeeId(employeeId);
                er.setRoleId(rid);
                employeeRoleMapper.insert(er);
            }
        }
    }

    @Override
    public List<Permission> listEmployeePermissions(Long employeeId) {
        List<EmployeeRole> ers = employeeRoleMapper.selectList(new LambdaQueryWrapper<EmployeeRole>().eq(EmployeeRole::getEmployeeId, employeeId));
        Set<Long> roleIds = ers.stream().map(EmployeeRole::getRoleId).collect(Collectors.toSet());
        if (roleIds.isEmpty()) return List.of();
        List<RolePermission> rps = rolePermissionMapper.selectList(new LambdaQueryWrapper<RolePermission>().in(RolePermission::getRoleId, roleIds));
        Set<Long> permIds = rps.stream().map(RolePermission::getPermissionId).collect(Collectors.toSet());
        if (permIds.isEmpty()) return List.of();
        return permissionMapper.selectBatchIds(permIds);
    }

    @Override
    public List<Permission> listAllPermissions() {
        return permissionMapper.selectList(null);
    }
}
