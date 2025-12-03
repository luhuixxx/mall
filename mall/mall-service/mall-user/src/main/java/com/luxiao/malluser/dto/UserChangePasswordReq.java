package com.luxiao.malluser.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserChangePasswordReq {
    @NotBlank
    private String oldPassword;
    @NotBlank
    private String newPassword;
}
