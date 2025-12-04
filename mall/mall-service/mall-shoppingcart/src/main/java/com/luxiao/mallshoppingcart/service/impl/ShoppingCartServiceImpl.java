package com.luxiao.mallshoppingcart.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.luxiao.mallcommon.api.ApiResponse;
import com.luxiao.mallfeignapi.procuct.ProductApi;
import com.luxiao.mallmodel.product.dto.UpdateStockReq;
import com.luxiao.mallmodel.product.vo.SkuInfoVO;
import com.luxiao.mallmodel.shoppingcart.CartItem;
import com.luxiao.mallmodel.product.Sku;
import com.luxiao.mallshoppingcart.dto.AddCartItemReq;
import com.luxiao.mallshoppingcart.mapper.CartItemMapper;
import com.luxiao.mallshoppingcart.service.ShoppingCartService;
import com.luxiao.mallshoppingcart.vo.CartItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShoppingCartServiceImpl extends ServiceImpl<CartItemMapper, CartItem> implements ShoppingCartService {

    private final ProductApi productApi;

    @Override
    @Transactional
    public CartItemVO addItem(AddCartItemReq req) {
        ResponseEntity<ApiResponse<Sku>> response = productApi.getSku(req.getSkuId());
        Sku sku = response.getBody().getData();
        if (sku == null) {
            throw new IllegalArgumentException("SKU不存在");
        }

        CartItem existing = this.getOne(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, req.getUserId())
                .eq(CartItem::getSkuId, req.getSkuId()));
        CartItem item;
        if (existing != null) {
            existing.setQuantity((existing.getQuantity() == null ? 0 : existing.getQuantity()) + req.getQuantity());
            this.updateById(existing);
            item = existing;
        } else {
            item = new CartItem();
            item.setUserId(req.getUserId());
            item.setSkuId(req.getSkuId());
            item.setSpuId(sku.getSpuId());
            item.setImageUrl(req.getImageUrl());
            item.setQuantity(req.getQuantity());
            this.save(item);
        }
        productApi.adjustStock(item.getSkuId(), new UpdateStockReq(-req.getQuantity()));
        return toVO(item, sku);
    }

    @Override
    @Transactional
    public boolean updateQuantity(Long id, Integer quantity) {
        CartItem item = this.getById(id);
        if (item == null) {
            throw new IllegalArgumentException("购物车项不存在");
        }
        if (quantity == null || quantity < 1) {
            throw new IllegalArgumentException("数量必须大于等于1");
        }
        int changeQue = item.getQuantity() - quantity;
        item.setQuantity(quantity);
        this.updateById(item);
        productApi.adjustStock(item.getSkuId(), new UpdateStockReq(changeQue ));
return true;
    }

    @Override
    @Transactional
    public boolean removeItem(Long id) {
        CartItem item = getById(id);
        if (item == null) {
            throw new IllegalArgumentException("购物车项不存在");
        }
        productApi.adjustStock(item.getSkuId(), new UpdateStockReq(item.getQuantity()));
        return  this.removeById(id);
    }

    @Override
    public List<CartItemVO> listItems(Long userId) {
        List<CartItem> items = this.list(new LambdaQueryWrapper<CartItem>().eq(CartItem::getUserId, userId));
        if (items.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> skuIds = items.stream().map(CartItem::getSkuId).collect(Collectors.toSet());
        ResponseEntity<ApiResponse<List<Sku>>> response = productApi.getBatch(skuIds);
        List<Sku> skus = response.getBody().getData();
        Map<Long, Sku> skuMap = skus.stream().collect(Collectors.toMap(Sku::getId, s -> s));
        return items.stream().map(i -> toVO(i, skuMap.get(i.getSkuId()))).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean clearCart(Long userId) {
        List<CartItem> items = this.list(new LambdaQueryWrapper<CartItem>().eq(CartItem::getUserId, userId));
        items.forEach(i -> productApi.adjustStock(i.getSkuId(), new UpdateStockReq(i.getQuantity())));
        return this.removeByIds( items.stream().map(CartItem::getId).toList());
    }

    private CartItemVO toVO(CartItem item, Sku sku) {
        CartItemVO vo = new CartItemVO();
        vo.setId(item.getId());
        vo.setUserId(item.getUserId());
        vo.setSpuId(item.getSpuId());
        vo.setImageUrl(item.getImageUrl());
        vo.setSkuId(item.getSkuId());
        vo.setQuantity(item.getQuantity());
        if (sku != null) {
            vo.setPrice(sku.getPrice());
            vo.setSpecification(sku.getSpecification());
        }
        return vo;
    }
}

