package com.luxiao.mallshoppingcart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddCartItemReq {
    @NotNull
    private Long userId;

    @NotNull
    private Long skuId;

    @NotNull
    @Min(1)
    private Integer quantity;

    private String imageUrl;
}

