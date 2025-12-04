package com.luxiao.mallorder.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class CreateOrderReq {
    @NotNull
    private Long userId;
    @NotNull
    private String name;
    @NotNull
    private String phone;
    @NotNull
    private String address;
    @NotEmpty
    private List<Item> items;

    @Data
    public static class Item {
        @NotNull
        private Long skuId;
        @NotNull
        private Integer quantity;
    }
}

