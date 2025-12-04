package com.luxiao.mallshoppingcart.service;

import com.luxiao.mallshoppingcart.dto.AddCartItemReq;
import com.luxiao.mallshoppingcart.vo.CartItemVO;
import java.util.List;

public interface ShoppingCartService {
    CartItemVO addItem(AddCartItemReq req);
    boolean updateQuantity(Long id, Integer quantity);
    boolean removeItem(Long id);
    List<CartItemVO> listItems(Long userId);
    boolean clearCart(Long userId);
}

