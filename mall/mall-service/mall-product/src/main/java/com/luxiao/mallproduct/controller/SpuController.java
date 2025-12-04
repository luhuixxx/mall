package com.luxiao.mallproduct.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luxiao.mallmodel.product.Spu;
import com.luxiao.mallcommon.api.ApiResponse;
import com.luxiao.mallproduct.dto.CreateSpuReq;
import com.luxiao.mallproduct.dto.UpdateSpuReq;
import com.luxiao.mallmodel.product.dto.UpdateSpuSalesReq;
import com.luxiao.mallproduct.service.SpuService;
import com.luxiao.mallproduct.service.storage.MinioStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/product/spu")
@Tag(name = "SPU APIs")
public class SpuController {

    private final SpuService spuService;
    private final MinioStorageService storageService;

    public SpuController(SpuService spuService, MinioStorageService storageService) {
        this.spuService = spuService;
        this.storageService = storageService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('product:write')")
    @Operation(summary = "创建SPU（图片上传）")
    public ResponseEntity<ApiResponse<Spu>> create(@Valid @ModelAttribute CreateSpuReq req,
                                                   @RequestPart("image") MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("图片文件不能为空");
        }
        Spu spu = new Spu();
        spu.setName(req.getName());
        spu.setDescription(req.getDescription());
        String imageUrl = storageService.uploadImage(image, "spu");
        spu.setImageUrl(imageUrl);
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

    @PutMapping("/{id}/sales")
    @PreAuthorize("hasAnyRole('INTERNAL_SERVICE')")
    @Operation(summary = "调整SPU销量")
    public ResponseEntity<ApiResponse<Void>> adjustSales(@PathVariable Long id, @Valid @RequestBody UpdateSpuSalesReq req) {
        spuService.adjustSales(id, req.getDelta());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/stats/top")
    @PreAuthorize("hasRole('EMPLOYEE')")
    @Operation(summary = "Top销量SPU")
    public ResponseEntity<ApiResponse<java.util.List<Spu>>> top(@RequestParam(defaultValue = "10") int limit) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Spu> p = spuService.page(com.baomidou.mybatisplus.extension.plugins.pagination.Page.of(1, limit), new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Spu>().orderByDesc(Spu::getSalesCount));
        return ResponseEntity.ok(ApiResponse.ok(p.getRecords()));
    }
}

