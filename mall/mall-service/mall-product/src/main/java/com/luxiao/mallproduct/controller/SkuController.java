package com.luxiao.mallproduct.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luxiao.mallmodel.product.Sku;
import com.luxiao.mallcommon.api.ApiResponse;
import com.luxiao.mallmodel.product.vo.SkuInfoVO;
import com.luxiao.mallproduct.dto.CreateSkuReq;
import com.luxiao.mallmodel.product.dto.UpdateStockReq;
import com.luxiao.mallproduct.service.SkuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/product/sku")
@Tag(name = "SKU APIs")
public class SkuController {

    private final SkuService skuService;

    public SkuController(SkuService skuService) {
        this.skuService = skuService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('product:write')")
    @Operation(summary = "创建SKU")
    public ResponseEntity<ApiResponse<Sku>> create(@Valid @RequestBody CreateSkuReq req) {
        Sku sku = new Sku();
        sku.setSpuId(req.getSpuId());
        sku.setPrice(req.getPrice());
        sku.setStockQuantity(req.getStockQuantity());
        sku.setStatus(req.getStatus());
        sku.setSpecification(req.getSpecification());
        skuService.save(sku);
        return ResponseEntity.ok(ApiResponse.ok(sku));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('product:write')")
    @Operation(summary = "更新SKU")
    public ResponseEntity<ApiResponse<Sku>> update(@PathVariable Long id, @RequestBody Sku req) {
        Sku sku = skuService.getById(id);
        if (sku == null) {
            throw new IllegalArgumentException("SKU不存在");
        }
        sku.setPrice(req.getPrice());
        sku.setStockQuantity(req.getStockQuantity());
        sku.setStatus(req.getStatus());
        sku.setSpecification(req.getSpecification());
        skuService.updateById(sku);
        return ResponseEntity.ok(ApiResponse.ok(sku));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询SKU")
    public ResponseEntity<ApiResponse<Sku>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(skuService.getById(id)));
    }
    @PostMapping("/batch")
    @Operation(summary = "批量查询SKU")
    public ResponseEntity<ApiResponse<List<Sku>>> getBatch(@RequestBody Set<Long> ids) {
        return ResponseEntity.ok(ApiResponse.ok(skuService.getBaseMapper().selectByIds(ids)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('product:write')")
    @Operation(summary = "删除SKU")
    public ResponseEntity<ApiResponse<Boolean>> delete(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(skuService.removeById(id)));
    }

    @GetMapping("/spu/{spuId}")
    @Operation(summary = "查询SPU下所有SKU")
    public ResponseEntity<ApiResponse<List<Sku>>> listBySpu(@PathVariable Long spuId) {
        LambdaQueryWrapper<Sku> qw = new LambdaQueryWrapper<>();
        qw.eq(Sku::getSpuId, spuId);
        return ResponseEntity.ok(ApiResponse.ok(skuService.list(qw)));
    }

    @PutMapping("/{id}/stock")
    @PreAuthorize("hasAuthority('product:write') or hasRole('INTERNAL_SERVICE')")
    @Operation(summary = "调整库存")
    public ResponseEntity<ApiResponse<Object>> adjustStock(@PathVariable Long id, @Valid @RequestBody UpdateStockReq req) {
        skuService.adjustStock(id, req.getDelta());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/info/{id}")
    @PreAuthorize("hasRole('INTERNAL_SERVICE')")
    public ResponseEntity<ApiResponse<SkuInfoVO>> getSkuInfo(@PathVariable Long id){
        return ResponseEntity.ok(ApiResponse.ok(skuService.getSkuInfo(id)));
    }

    @GetMapping("/stats/low-stock")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "低库存SKU")
    public ResponseEntity<ApiResponse<List<Sku>>> lowStock(@RequestParam(defaultValue = "10") int threshold,
                                                           @RequestParam(defaultValue = "10") int limit) {
        LambdaQueryWrapper<Sku> qw = new LambdaQueryWrapper<>();
        qw.lt(Sku::getStockQuantity, threshold).orderByAsc(Sku::getStockQuantity);
        var page = skuService.page(Page.of(1, limit), qw);
        return ResponseEntity.ok(ApiResponse.ok(page.getRecords()));
    }
}
