package com.luxiao.mallproduct.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateStockReq {
    @NotNull
    private Integer delta;
}

