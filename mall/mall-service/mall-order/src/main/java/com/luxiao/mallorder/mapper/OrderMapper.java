package com.luxiao.mallorder.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.luxiao.mallmodel.order.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}

