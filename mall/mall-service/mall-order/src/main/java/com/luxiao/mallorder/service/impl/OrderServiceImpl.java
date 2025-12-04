package com.luxiao.mallorder.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luxiao.mallcommon.api.ApiResponse;
import com.luxiao.mallfeignapi.procuct.ProductApi;
import com.luxiao.mallmodel.order.Order;
import com.luxiao.mallmodel.order.OrderDetail;
import com.luxiao.mallmodel.product.Sku;
import com.luxiao.mallmodel.product.dto.UpdateSpuSalesReq;
import com.luxiao.mallmodel.product.vo.SkuInfoVO;
import com.luxiao.mallmodel.user.dto.UserAdjustBalanceReq;
import com.luxiao.mallorder.config.RabbitConfig;
import com.luxiao.mallorder.dto.CreateOrderReq;
import com.luxiao.mallfeignapi.user.UserApi;
import com.luxiao.mallorder.mapper.OrderDetailMapper;
import com.luxiao.mallorder.mapper.OrderMapper;

import com.luxiao.mallorder.service.OrderService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luxiao.mallorder.vo.OrderStatsResp;

import java.math.BigDecimal;
import java.util.*;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final OrderDetailMapper orderDetailMapper;
    private final ProductApi productApi;
    private final UserApi userApi;
    private final RabbitTemplate rabbitTemplate;

    public OrderServiceImpl(OrderDetailMapper orderDetailMapper, ProductApi productApi, UserApi userApi, RabbitTemplate rabbitTemplate) {
        this.orderDetailMapper = orderDetailMapper;
        this.productApi = productApi;
        this.userApi = userApi;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    @Transactional
    public Order createOrder(CreateOrderReq req) {
        long orderId = System.currentTimeMillis();
        Order order = new Order();
        order.setId(orderId);
        order.setUserId(req.getUserId());
        order.setName(req.getName());
        order.setPhone(req.getPhone());
        order.setAddress(req.getAddress());
        order.setStatus(0);
        int totalQty = 0;
        BigDecimal amount = BigDecimal.ZERO;
        Map<Long, Integer> spuSales = new HashMap<>();
        for (CreateOrderReq.Item it : req.getItems()) {
            ApiResponse<SkuInfoVO> resp = productApi.getSkuInfo(it.getSkuId()).getBody();
            SkuInfoVO sku = resp.getData();
            if (sku == null) {
                throw new IllegalArgumentException("SKU不存在");
            }
            BigDecimal line = sku.getPrice().multiply(BigDecimal.valueOf(it.getQuantity()));
            amount = amount.add(line);
            totalQty += it.getQuantity();
            OrderDetail od = new OrderDetail();
            od.setOrderId(orderId);
            od.setSkuId(it.getSkuId());
            od.setQuantity(it.getQuantity());
            od.setPrice(sku.getPrice());
            od.setImgUrl(sku.getImgUrl());
            od.setSpuName(sku.getSpuName());
            od.setSpecification(sku.getSpecification());
            orderDetailMapper.insert(od);
            spuSales.merge(sku.getSpuId(), it.getQuantity(), Integer::sum);
        }
        order.setTotalQuantity(totalQty);
        order.setAmount(amount);
        this.save(order);

        MessageProperties mp = new MessageProperties();
        mp.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        Message msg = new Message(String.valueOf(orderId).getBytes(), mp);
        rabbitTemplate.send(RabbitConfig.EXCHANGE_EVENTS, RabbitConfig.RK_DELAY, msg);

        return order;
    }

    @Override
    @Transactional
    public boolean cancelOrder(Long orderId) {
        Order order = this.getById(orderId);
        if (order == null) {
            return false;
        }
        if (order.getStatus() != null && order.getStatus() == 1) {
            return false;
        }
        order.setStatus(2);
        return this.updateById(order);
    }

    @Override
    @Transactional
    public boolean payOrder(Long orderId, Long userId) {
        Order order = this.getById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (!Objects.equals(order.getUserId(), userId)) {
            throw new IllegalArgumentException("非法用户");
        }
        if (order.getStatus() != null && order.getStatus() == 1) {
            return true;
        }
        if (order.getStatus() != null && order.getStatus() == 2) {
            throw new IllegalArgumentException("订单已取消");
        }

        UserAdjustBalanceReq balReq = new UserAdjustBalanceReq();
        balReq.setDelta(order.getAmount().negate());
        ApiResponse<?> balResp = userApi.adjustBalance(userId, balReq).getBody();
        if (balResp.getCode() != 0) {
            throw new IllegalArgumentException("扣减余额失败:" + balResp.getMessage());
        }

        List<OrderDetail> details = orderDetailMapper.selectList(new LambdaQueryWrapper<OrderDetail>().eq(OrderDetail::getOrderId, orderId));
        Map<Long, Integer> spuSales = new HashMap<>();
        for (OrderDetail d : details) {
            ApiResponse<Sku> resp = productApi.getSku(d.getSkuId()).getBody();
            Sku sku = resp.getData();
            if (sku != null) {
                spuSales.merge(sku.getSpuId(), d.getQuantity(), Integer::sum);
            }
        }
        for (Map.Entry<Long, Integer> e : spuSales.entrySet()) {
            UpdateSpuSalesReq sreq = new UpdateSpuSalesReq();
            sreq.setDelta(e.getValue());
            productApi.adjustSpuSales(e.getKey(), sreq);
        }

        order.setStatus(1);
        return this.updateById(order);
    }

    @Override
    public List<Order> listUserOrders(Long userId) {
        return this.list(new LambdaQueryWrapper<Order>().eq(Order::getUserId, userId));
    }

    @Override
    public List<OrderDetail> getOrderDetails(Long orderId) {
        Order order = this.getById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        return orderDetailMapper.selectList(new LambdaQueryWrapper<OrderDetail>().eq(OrderDetail::getOrderId, orderId));
    }

    @Override
    public Page<Order> pageOrders(int page, int size, Long userId, Integer status, String name, String phone, String trackingNumber, Long orderId) {
        LambdaQueryWrapper<Order> qw = new LambdaQueryWrapper<>();
        qw.eq(userId != null, Order::getUserId, userId)
          .eq(status != null, Order::getStatus, status)
          .like(name != null && !name.isBlank(), Order::getName, name)
          .like(phone != null && !phone.isBlank(), Order::getPhone, phone)
          .like(trackingNumber != null && !trackingNumber.isBlank(), Order::getTrackingNumber, trackingNumber)
          .eq(orderId != null, Order::getId, orderId)
          .orderByDesc(Order::getCreatedTime);
        return this.page(Page.of(page, size), qw);
    }

    @Override
    public OrderStatsResp statsSummary() {
        OrderStatsResp resp = new OrderStatsResp();
        long total = this.count();
        long paid = this.count(new LambdaQueryWrapper<Order>().eq(Order::getStatus, 1));
        var now = java.time.LocalDateTime.now();
        var startToday = now.withHour(0).withMinute(0).withSecond(0).withNano(0);
        var start7 = now.minusDays(7);
        java.util.List<Order> today = this.list(new LambdaQueryWrapper<Order>().ge(Order::getCreatedTime, startToday));
        java.util.List<Order> last7 = this.list(new LambdaQueryWrapper<Order>().ge(Order::getCreatedTime, start7).eq(Order::getStatus, 1));
        java.math.BigDecimal todayAmount = today.stream().filter(o -> o.getStatus() != null && o.getStatus() == 1)
                .map(o -> o.getAmount() == null ? java.math.BigDecimal.ZERO : o.getAmount())
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        java.math.BigDecimal last7Amount = last7.stream()
                .map(o -> o.getAmount() == null ? java.math.BigDecimal.ZERO : o.getAmount())
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        resp.setTotalOrders(total);
        resp.setPaidOrders(paid);
        resp.setTodayOrders(today.size());
        resp.setTodayAmount(todayAmount);
        resp.setLast7DaysAmount(last7Amount);
        return resp;
    }

    @Override
    public List<Order> recent(int limit) {
        Page<Order> page = this.page(Page.of(1, limit), new LambdaQueryWrapper<Order>().orderByDesc(Order::getCreatedTime));
        return page.getRecords();
    }
}
