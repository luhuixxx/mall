package com.luxiao.mallshoppingcart.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CartItemVO {
    private Long id;
    private Long userId;
    private Long spuId;
    private String imageUrl;
    private Long skuId;
    private Integer quantity;

    private BigDecimal price;
    private String specification;
}

