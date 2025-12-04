package com.luxiao.mallorder.controller;

import com.luxiao.mallcommon.api.ApiResponse;
import com.luxiao.mallmodel.order.Order;
import com.luxiao.mallmodel.order.OrderDetail;
import com.luxiao.mallorder.dto.CreateOrderReq;
import com.luxiao.mallorder.dto.PayOrderReq;
import com.luxiao.mallorder.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luxiao.mallorder.vo.OrderStatsResp;

import java.util.List;

@RestController
@RequestMapping("/api/order")
@Tag(name = "Order APIs")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "新增订单")
    public ResponseEntity<ApiResponse<Order>> create(@Valid @RequestBody CreateOrderReq req) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.createOrder(req)));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('USER','EMPLOYEE','INTERNAL_SERVICE')")
    @Operation(summary = "取消订单")
    public ResponseEntity<ApiResponse<Boolean>> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.cancelOrder(id)));
    }

    @PutMapping("/{id}/pay")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "支付订单")
    public ResponseEntity<ApiResponse<Boolean>> pay(@PathVariable Long id, @Valid @RequestBody PayOrderReq req) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.payOrder(id, req.getUserId())));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('USER','EMPLOYEE')")
    @Operation(summary = "查询用户订单")
    public ResponseEntity<ApiResponse<List<Order>>> listByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.listUserOrders(userId)));
    }

    @GetMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "分页查询所有订单")
    public ResponseEntity<ApiResponse<Page<Order>>> page(@RequestParam(defaultValue = "1") int page,
                                                         @RequestParam(defaultValue = "10") int size,
                                                         @RequestParam(required = false) Long userId,
                                                         @RequestParam(required = false) Integer status,
                                                         @RequestParam(required = false) String name,
                                                         @RequestParam(required = false) String phone,
                                                         @RequestParam(required = false) String trackingNumber,
                                                         @RequestParam(required = false) Long orderId) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.pageOrders(page, size, userId, status, name, phone, trackingNumber, orderId)));
    }

    @GetMapping("/stats/summary")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "订单统计汇总")
    public ResponseEntity<ApiResponse<OrderStatsResp>> statsSummary() {
        return ResponseEntity.ok(ApiResponse.ok(orderService.statsSummary()));
    }

    @GetMapping("/recent")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "最近订单")
    public ResponseEntity<ApiResponse<java.util.List<Order>>> recent(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.recent(limit)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','EMPLOYEE')")
    @Operation(summary = "查询订单详情")
    public ResponseEntity<ApiResponse<List<OrderDetail>>> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getOrderDetails(id)));
    }

    
}
