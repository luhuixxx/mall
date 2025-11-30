package com.luxiao.mallproduct.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateSkuReq {
    @NotNull
    private Long spuId;
    @NotNull
    private BigDecimal price;
    @NotNull
    private Integer stockQuantity;
    private Integer status;
    private String specification;
}

