package com.luxiao.mallmodel.product.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SkuInfoVO {
    private Long id;

    private Long spuId;

    private BigDecimal price;

    private Integer stockQuantity;

    private Integer status;

    private String specification;


    private String spuName;

    private String imgUrl;
}
