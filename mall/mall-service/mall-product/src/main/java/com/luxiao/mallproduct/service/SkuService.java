package com.luxiao.mallproduct.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.luxiao.mallmodel.product.Sku;

public interface SkuService extends IService<Sku> {
    void adjustStock(Long skuId, int delta);
}

