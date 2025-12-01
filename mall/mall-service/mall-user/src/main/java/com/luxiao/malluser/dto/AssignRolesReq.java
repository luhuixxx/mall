package com.luxiao.malluser.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public class AssignRolesReq {
    @NotNull
    private Long employeeId;
    @NotNull
    private List<Long> roleIds;

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public List<Long> getRoleIds() { return roleIds; }
    public void setRoleIds(List<Long> roleIds) { this.roleIds = roleIds; }
}
