package com.luxiao.mallorder.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.luxiao.mallmodel.order.Order;
import com.luxiao.mallorder.dto.CreateOrderReq;
import com.luxiao.mallmodel.order.OrderDetail;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luxiao.mallorder.vo.OrderStatsResp;
import java.util.List;

public interface OrderService extends IService<Order> {
    Order createOrder(CreateOrderReq req);
    boolean cancelOrder(Long orderId);
    boolean payOrder(Long orderId, Long userId);
    List<Order> listUserOrders(Long userId);
    java.util.List<OrderDetail> getOrderDetails(Long orderId);
    Page<Order> pageOrders(int page, int size, Long userId, Integer status, String name, String phone, String trackingNumber, Long orderId);
    OrderStatsResp statsSummary();
    java.util.List<Order> recent(int limit);
}
