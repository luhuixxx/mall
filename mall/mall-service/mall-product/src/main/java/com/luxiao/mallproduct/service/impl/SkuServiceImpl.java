package com.luxiao.mallproduct.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luxiao.mallmodel.product.Sku;
import com.luxiao.mallproduct.mapper.SkuMapper;
import com.luxiao.mallproduct.service.SkuService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SkuServiceImpl extends ServiceImpl<SkuMapper, Sku> implements SkuService {

    @Override
    @Transactional
    public void adjustStock(Long skuId, int delta) {
        Sku sku = this.getById(skuId);
        if (sku == null) {
            throw new IllegalArgumentException("SKU不存在");
        }
        int newStock = (sku.getStockQuantity() == null ? 0 : sku.getStockQuantity()) + delta;
        if (newStock < 0) {
            throw new IllegalArgumentException("库存不足");
        }
        sku.setStockQuantity(newStock);
        this.updateById(sku);
    }
}

