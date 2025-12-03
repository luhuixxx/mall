package com.luxiao.malluser.vo;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EmployeeVO {
    private Long id;

    private String username;

    private String password;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;

    private String createdUser;

    private String updatedUser;

    private String roleName;
}
