package com.luxiao.mallproduct.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luxiao.mallmodel.product.Spu;
import com.luxiao.mallcommon.api.ApiResponse;
import com.luxiao.mallproduct.dto.CreateSpuReq;
import com.luxiao.mallproduct.dto.UpdateSpuReq;
import com.luxiao.mallproduct.service.SpuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/product/spu")
@Tag(name = "SPU APIs")
public class SpuController {

    private final SpuService spuService;

    public SpuController(SpuService spuService) {
        this.spuService = spuService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('product:write')")
    @Operation(summary = "创建SPU")
    public ResponseEntity<ApiResponse<Spu>> create(@Valid @RequestBody CreateSpuReq req) {
        Spu spu = new Spu();
        spu.setName(req.getName());
        spu.setDescription(req.getDescription());
        spu.setImageUrl(req.getImageUrl());
        spuService.save(spu);
        return ResponseEntity.ok(ApiResponse.ok(spu));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('product:write')")
    @Operation(summary = "更新SPU")
    public ResponseEntity<ApiResponse<Spu>> update(@PathVariable Long id, @Valid @RequestBody UpdateSpuReq req) {
        Spu spu = spuService.getById(id);
        if (spu == null) {
            throw new IllegalArgumentException("SPU不存在");
        }
        spu.setName(req.getName());
        spu.setDescription(req.getDescription());
        spu.setImageUrl(req.getImageUrl());
        spuService.updateById(spu);
        return ResponseEntity.ok(ApiResponse.ok(spu));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询SPU")
    public ResponseEntity<ApiResponse<Spu>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(spuService.getById(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('product:write')")
    @Operation(summary = "删除SPU")
    public ResponseEntity<ApiResponse<Boolean>> delete(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(spuService.removeById(id)));
    }

    @GetMapping
    @Operation(summary = "分页查询SPU")
    public ResponseEntity<ApiResponse<Page<Spu>>> page(@RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "10") int size,
                                          @RequestParam(required = false) String name) {
        return ResponseEntity.ok(ApiResponse.ok(spuService.pageSpu(page, size, name)));
    }
}

