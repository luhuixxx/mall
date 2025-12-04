package com.luxiao.mallorder.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE_EVENTS = "order.events";
    public static final String EXCHANGE_DLX = "order.dlx";
    public static final String QUEUE_DELAY = "order.payment.timeout.delay";
    public static final String QUEUE_CANCEL = "order.cancel.queue";
    public static final String RK_DELAY = "order.payment.timeout";
    public static final String RK_CANCEL = "order.cancel";

    @Bean
    public DirectExchange orderEventsExchange() {
        return new DirectExchange(EXCHANGE_EVENTS);
    }

    @Bean
    public DirectExchange orderDlxExchange() {
        return new DirectExchange(EXCHANGE_DLX);
    }

    @Bean
    public Queue delayQueue() {
        Map<String, Object> args = new java.util.HashMap<>();
        args.put("x-dead-letter-exchange", EXCHANGE_DLX);
        args.put("x-dead-letter-routing-key", RK_CANCEL);
        args.put("x-message-ttl", 300000); // 5分钟
        return new Queue(QUEUE_DELAY, true, false, false, args);
    }

    @Bean
    public Queue cancelQueue() {
        return new Queue(QUEUE_CANCEL, true);
    }

    @Bean
    public Binding delayBinding() {
        return BindingBuilder.bind(delayQueue()).to(orderEventsExchange()).with(RK_DELAY);
    }

    @Bean
    public Binding cancelBinding() {
        return BindingBuilder.bind(cancelQueue()).to(orderDlxExchange()).with(RK_CANCEL);
    }
}

