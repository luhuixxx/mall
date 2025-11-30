package com.luxiao.malluser.dto;

import jakarta.validation.constraints.NotBlank;

public class EmployeeUpdateReq {
    @NotBlank
    private String username;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}

