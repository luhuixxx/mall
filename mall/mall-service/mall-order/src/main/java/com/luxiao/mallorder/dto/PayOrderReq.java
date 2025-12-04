package com.luxiao.mallorder.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PayOrderReq {
    @NotNull
    private Long userId;
}

