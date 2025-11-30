package com.luxiao.mallproduct;

import com.luxiao.mallproduct.dto.CreateSkuReq;
import com.luxiao.mallproduct.dto.CreateSpuReq;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class ProductDtoTests {
    @Test
    void createSpuReq_basicFields() {
        CreateSpuReq req = new CreateSpuReq();
        req.setName("n");
        req.setDescription("d");
        req.setImageUrl("i");
        assertEquals("n", req.getName());
        assertEquals("d", req.getDescription());
        assertEquals("i", req.getImageUrl());
    }

    @Test
    void createSkuReq_basicFields() {
        CreateSkuReq req = new CreateSkuReq();
        req.setSpuId(1L);
        req.setPrice(new BigDecimal("9.99"));
        req.setStockQuantity(10);
        req.setStatus(1);
        req.setSpecification("spec");
        assertEquals(1L, req.getSpuId());
        assertEquals(new BigDecimal("9.99"), req.getPrice());
        assertEquals(10, req.getStockQuantity());
        assertEquals(1, req.getStatus());
        assertEquals("spec", req.getSpecification());
    }
}

