package com.luxiao.mallorder.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.luxiao.mallmodel.order.OrderDetail;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderDetailMapper extends BaseMapper<OrderDetail> {
}

