package com.luxiao.mallmodel.product.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateSpuSalesReq {
    @NotNull
    private Integer delta;
}

