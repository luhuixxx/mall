package com.luxiao.malluser.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserLoginReq {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
}

