package com.luxiao.mallshoppingcart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateQuantityReq {
    @NotNull
    @Min(1)
    private Integer quantity;
}

