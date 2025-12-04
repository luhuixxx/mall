package com.luxiao.mallproduct.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luxiao.mallmodel.product.Sku;
import com.luxiao.mallmodel.product.Spu;
import com.luxiao.mallmodel.product.vo.SkuInfoVO;
import com.luxiao.mallproduct.mapper.SkuMapper;
import com.luxiao.mallproduct.mapper.SpuMapper;
import com.luxiao.mallproduct.service.SkuService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SkuServiceImpl extends ServiceImpl<SkuMapper, Sku> implements SkuService {

    private final SpuMapper spuMapper;

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

    @Override
    public SkuInfoVO getSkuInfo(Long id) {
        Sku sku = this.getById(id);
        if (sku == null) {
            throw new IllegalArgumentException("SKU不存在");
        }
        Spu spu = spuMapper.selectById(sku.getSpuId());
        SkuInfoVO skuInfoVO = new SkuInfoVO();
        BeanUtils.copyProperties(sku, skuInfoVO);
        skuInfoVO.setImgUrl(spu.getImageUrl());
        skuInfoVO.setSpuName(spu.getName());
        return skuInfoVO;
    }
}

