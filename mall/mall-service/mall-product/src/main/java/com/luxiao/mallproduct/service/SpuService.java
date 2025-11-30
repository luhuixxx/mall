package com.luxiao.mallproduct.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.luxiao.mallmodel.product.Spu;

public interface SpuService extends IService<Spu> {
    Page<Spu> pageSpu(int page, int size, String name);
}

