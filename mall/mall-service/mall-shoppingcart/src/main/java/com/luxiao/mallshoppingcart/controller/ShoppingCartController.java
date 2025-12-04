package com.luxiao.mallshoppingcart.controller;

import com.luxiao.mallcommon.api.ApiResponse;
import com.luxiao.mallshoppingcart.dto.AddCartItemReq;
import com.luxiao.mallshoppingcart.dto.UpdateQuantityReq;
import com.luxiao.mallshoppingcart.service.ShoppingCartService;
import com.luxiao.mallshoppingcart.vo.CartItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shoppingcart")
@Tag(name = "Shopping Cart")
public class ShoppingCartController {

    private final ShoppingCartService shoppingCartService;

    public ShoppingCartController(ShoppingCartService shoppingCartService) {
        this.shoppingCartService = shoppingCartService;
    }

    @PostMapping("/item")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "添加购物车项")
    public ResponseEntity<ApiResponse<CartItemVO>> addItem(@Valid @RequestBody AddCartItemReq req) {
        CartItemVO vo = shoppingCartService.addItem(req);
        return ResponseEntity.ok(ApiResponse.ok(vo));
    }

    @PutMapping("/item/{id}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "更新购物车项数量")
    public ResponseEntity<ApiResponse<Boolean>> updateQuantity(@PathVariable Long id, @Valid @RequestBody UpdateQuantityReq req) {

        return ResponseEntity.ok(ApiResponse.ok(shoppingCartService.updateQuantity(id, req.getQuantity())));
    }

    @DeleteMapping("/item/{id}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "删除购物车项")
    public ResponseEntity<ApiResponse<Boolean>> removeItem(@PathVariable Long id) {
        boolean ok = shoppingCartService.removeItem(id);
        return ResponseEntity.ok(ApiResponse.ok(ok));
    }

    @GetMapping("/items")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "查询用户购物车")
    public ResponseEntity<ApiResponse<List<CartItemVO>>> listItems(@RequestParam Long userId) {
        List<CartItemVO> list = shoppingCartService.listItems(userId);
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @DeleteMapping("/clear")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "清空用户购物车")
    public ResponseEntity<ApiResponse<Boolean>> clearCart(@RequestParam Long userId) {
        boolean ok = shoppingCartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.ok(ok));
    }
}