package com.luxiao.mallorder.vo;

import com.luxiao.mallmodel.order.Order;
import com.luxiao.mallmodel.order.OrderDetail;
import lombok.Data;
import java.util.List;

@Data
public class OrderDetailsResp {
    private Order order;
    private List<OrderDetail> items;
}

