package com.luxiao.malluser.dto;

import com.luxiao.mallmodel.user.Employee;
import java.util.List;

public class EmployeeLoginResp {
    private String token;
    private Employee employee;
    private List<String> roles;
    private List<String> permissions;

    public EmployeeLoginResp() {}

    public EmployeeLoginResp(String token, Employee employee, List<String> roles, List<String> permissions) {
        this.token = token;
        this.employee = employee;
        this.roles = roles;
        this.permissions = permissions;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }
}

