package com.luxiao.malluser.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserUpdateReq {
    @NotBlank
    private String name;
    @Email
    @NotBlank
    private String email;
}

