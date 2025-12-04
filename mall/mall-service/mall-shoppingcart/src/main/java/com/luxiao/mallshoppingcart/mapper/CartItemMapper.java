package com.luxiao.mallshoppingcart.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.luxiao.mallmodel.shoppingcart.CartItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CartItemMapper extends BaseMapper<CartItem> {
}
