package com.luxiao.mallorder.mq;

import com.luxiao.mallorder.config.RabbitConfig;
import com.luxiao.mallorder.service.OrderService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OrderCancelListener {

    private final OrderService orderService;

    public OrderCancelListener(OrderService orderService) {
        this.orderService = orderService;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_CANCEL)
    public void onCancelMessage(String orderIdStr) {
        try {
            Long orderId = Long.valueOf(orderIdStr);
            orderService.cancelOrder(orderId);
        } catch (Exception ignored) {
        }
    }
}

