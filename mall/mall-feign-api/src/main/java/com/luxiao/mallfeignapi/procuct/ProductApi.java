package com.luxiao.mallfeignapi.procuct;


import com.luxiao.mallcommon.api.ApiResponse;
import com.luxiao.mallfeignapi.config.InternalAuthFeignInterceptorConfig;
import com.luxiao.mallmodel.product.Sku;
import com.luxiao.mallmodel.product.dto.UpdateSpuSalesReq;
import com.luxiao.mallmodel.product.dto.UpdateStockReq;
import com.luxiao.mallmodel.product.vo.SkuInfoVO;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@FeignClient(name = "mall-product",configuration = InternalAuthFeignInterceptorConfig.class)
public interface ProductApi {
    @GetMapping("/api/product/sku/info/{id}")
    public ResponseEntity<ApiResponse<SkuInfoVO>> getSkuInfo(@PathVariable Long id);

    @GetMapping("/api/product/sku//{id}")
    public ResponseEntity<ApiResponse<Sku>> getSku(@PathVariable Long id);

    @PostMapping("/api/product/sku/batch")
    public ResponseEntity<ApiResponse<List<Sku>>> getBatch(@RequestBody Set<Long> ids);

    @PutMapping("/api/product/sku/{id}/stock")
    public ResponseEntity<ApiResponse<Object>> adjustStock(@PathVariable Long id, @RequestBody UpdateStockReq req);

    @PutMapping("/api/product/spu/{id}/sales")
    public ResponseEntity<ApiResponse<Void>> adjustSpuSales(@PathVariable Long id, @RequestBody UpdateSpuSalesReq req);
}